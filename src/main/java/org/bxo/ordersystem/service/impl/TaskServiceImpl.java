package org.bxo.ordersystem.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.api.model.ItemInfo;
import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.api.model.OrderItem;
import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private ItemService itemService;

    @Value("${org.jobrunr.background-job-server.worker_count}")
    private Long workerCount;

    private static ConcurrentHashMap<UUID, OrderDetail> orderMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<UUID, ItemInfo> itemMap = new ConcurrentHashMap<>();

    private static AtomicLong availableEpochMillis = new AtomicLong(0L);

    @Override
    public void acceptOrder(OrderInfo order) {
	UUID orderId = order.getOrderId();
	if (null != orderMap.putIfAbsent(
		orderId, new OrderDetail(orderId, order.getItemList()))) {
	    System.err.printf("Duplicate order %s%n", orderId);
	    return;
	}
	System.out.printf("Accept order %s%n", orderId);

	Long epochMillis = System.currentTimeMillis();
	Long availableTime = availableEpochMillis.get();
	while (availableTime < epochMillis) {
	    availableEpochMillis.compareAndSet(availableTime, epochMillis);
	    availableTime = availableEpochMillis.get();
	}

	long orderPrepareMillis = 0L;
	for (OrderItem item : order.getItemList()) {
	    long itemPrepareMillis = getItemPrepareMillis(item.getItemId()) * item.getQuantity();
	    orderPrepareMillis += itemPrepareMillis;
	}
	if (orderPrepareMillis > 0L) {
	    availableTime = availableEpochMillis.addAndGet(
		orderPrepareMillis / workerCount);
	}

	for (OrderItem item : order.getItemList()) {
	    UUID itemId = item.getItemId();
	    long itemScheduleSeconds = (
		availableTime - getItemPrepareMillis(item.getItemId())
		- epochMillis ) / 1000L;
	    for (int i=0; i < item.getQuantity(); i++) {
		if (itemScheduleSeconds > 0) {
		    jobScheduler.schedule(
			() -> this.prepareItem(orderId, itemId),
			LocalDateTime.now().plusSeconds(itemScheduleSeconds));
		} else {
		    jobScheduler.enqueue(() -> this.prepareItem(orderId, itemId));
		}
	    }
	}

	// Check whether order is ready for delivery
	this.checkOrder(orderId);
    }

    @Override
    public void prepareItem(UUID orderId, UUID itemId) {
	if (!orderMap.containsKey(orderId)) {
	    System.err.printf("Missing order %s to prepare item %s%n", orderId, itemId);
	    return;
	}
	OrderDetail order = orderMap.get(orderId);
	ItemDetail item = order.getItemDetail(itemId);
	if (null == item) {
	    System.err.printf("Order %s missing item %s to prepare%n", orderId, itemId);
	    return;
	}

	Long prepareMillis = getItemPrepareMillis(itemId);
	if (prepareMillis > 0) {
	    try {
		Thread.sleep(prepareMillis);
	    } catch (InterruptedException e) {
	    }
	}
	long newQty = item.addPreparedQty(1);
	System.out.printf(
		"Order %s prepared item %s qty %d of %d%n", orderId, itemId,
		newQty, item.getQuantity());

	if (newQty >= item.getQuantity()) {
	    this.checkOrder(orderId);
	}
    }

    private void checkOrder(UUID orderId) {
	OrderDetail order = orderMap.get(orderId);
	if (null == order) {
	    System.err.printf("Missing order %s for checking%n", orderId);
	    return;
	}

	boolean allPrepared = true;
	for (ItemDetail i : order.getItemList()) {
	    if (i.getPreparedQty() < i.getQuantity()) {
		allPrepared = false;
		break;
	    }
	}
	if (allPrepared) {
	    jobScheduler.enqueue(() -> this.deliverOrder(orderId));
	}
    }

    @Override
    public void deliverOrder(UUID orderId) {
	OrderDetail order = orderMap.remove(orderId);
	if (null == order) {
	    System.err.printf("Missing order %s for delivery%n", orderId);
	    return;
	}
	System.out.printf("Deliver order %s%n", orderId);
	for (ItemDetail i : order.getItemList()) {
	    UUID itemId = i.getItemId();
	    if (itemMap.containsKey(itemId)) {
		long expiredQty = i.getExpiredQty(itemMap.get(itemId).getExpiryTimeMillis());
		if (expiredQty > 0) {
		    System.out.printf("Order %s item %s expired Qty %d%n", orderId, itemId, expiredQty);
		}
	    }
	}
    }

    private Long getItemPrepareMillis(UUID itemId) {
	if (!itemMap.containsKey(itemId)) {
	    itemMap.putIfAbsent(itemId, itemService.getItem(itemId));
	}
	if (itemMap.containsKey(itemId)) {
	    return itemMap.get(itemId).getPrepareTimeMillis();
	}
	return 0L;
    }

    private Callable<String> createTask(int i) {
	return () -> {
	    System.out.printf("running task %d. Thread: %s%n",
			      i,
			      Thread.currentThread().getName());
	    return String.format("Task finished %d", i);
	};
    }

}

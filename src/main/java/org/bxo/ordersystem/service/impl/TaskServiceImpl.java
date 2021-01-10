package org.bxo.ordersystem.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.api.model.ItemInfo;
import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.CourierService;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private CourierService courierService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderRepository orderRepo;

    @Value("${org.jobrunr.background-job-server.worker_count}")
    private Long workerCount;

    private static ConcurrentHashMap<UUID, ItemInfo> itemCache = new ConcurrentHashMap<>();

    private static AtomicLong availableEpochMillis = new AtomicLong(0L);

    @Override
    public void acceptOrder(UUID orderId) {
	OrderDetail order = orderRepo.getOrder(orderId);
	if (null == order) {
	    System.err.printf("TaskSvc: acceptOrder: Missing order %s%n", orderId);
	    return;
	}
	if (!order.isPlacedOrder()) {
	    System.err.printf("TaskSvc: acceptOrder: Order %s not submitted%n", orderId);
	    return;
	}
	System.out.printf("Accept order %s%n", orderId);

	Long availableTime = availableEpochMillis.get();
	Long epochMillis = System.currentTimeMillis();
	while (availableTime < epochMillis) {
	    availableEpochMillis.compareAndSet(availableTime, epochMillis);
	    availableTime = availableEpochMillis.get();
	}

	long orderPrepareMillis = 0L;
	for (ItemDetail item : order.getItemList()) {
	    long itemPrepareMillis = getItemPrepareMillis(item.getItemId()) * item.getQuantity();
	    orderPrepareMillis += itemPrepareMillis;
	}
	if (orderPrepareMillis > 0L) {
	    availableTime = availableEpochMillis.addAndGet(
		orderPrepareMillis / workerCount);
	}

	for (ItemDetail item : order.getItemList()) {
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
	OrderDetail order = orderRepo.getOrder(orderId);
	if (null == order) {
	    System.err.printf("TaskSvc: prepareItem: Missing order %s to prepare item %s%n", orderId, itemId);
	    return;
	}
	if (!order.isPlacedOrder()) {
	    System.err.printf("TaskSvc: prepareItem: Order %s not yet submitted%n", orderId);
	    return;
	}
	ItemDetail item = order.getItemDetail(itemId);
	if (null == item) {
	    System.err.printf("TaskSvc: prepareItem: Order %s missing item %s to prepare%n", orderId, itemId);
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
	OrderDetail order = orderRepo.getOrder(orderId);
	if (null == order) {
	    System.err.printf("TaskSvc: checkOrder: Missing order %s for checking%n", orderId);
	    return;
	}
	if (!order.isPlacedOrder()) {
	    System.err.printf("TaskSvc: checkOrder: Order %s not yet submitted%n", orderId);
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
	    courierService.readyOrder(orderId);
	}
    }

    private Long getItemPrepareMillis(UUID itemId) {
	if (!itemCache.containsKey(itemId)) {
	    itemCache.putIfAbsent(itemId, itemService.getItem(itemId));
	}
	if (itemCache.containsKey(itemId)) {
	    return itemCache.get(itemId).getPrepareTimeMillis();
	}
	return 0L;
    }

}

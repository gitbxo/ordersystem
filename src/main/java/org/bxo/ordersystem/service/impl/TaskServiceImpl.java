package org.bxo.ordersystem.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
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

    @Value("${ordersystem.courier.min_time_seconds:3}")
    private Long courierMinSeconds;

    @Value("${ordersystem.courier.max_time_seconds:15}")
    private Long courierMaxSeconds;

    @Value("${org.jobrunr.background-job-server.worker_count}")
    private Long workerCount;

    private static ConcurrentHashMap<UUID, ItemInfo> itemCache = new ConcurrentHashMap<>();

    @Override
    public void acceptOrder(UUID orderId) {
	System.out.printf("TaskSvc: Accept order %s%n", orderId);
	OrderDetail order = orderRepo.placeOrder(orderId);
	if (null == order) {
	    System.err.printf("TaskSvc: acceptOrder: Missing order %s%n", orderId);
	    return;
	}

	// Schedule courier pickup when order is placed
	scheduleCourier(orderId);

	// Calculate the approx time when order will be ready
	long averagePrepareTime = calculateAveragePrepareTime(order);

	// Schedule the items to be prepared
	schedulePrepareOrder(order, averagePrepareTime);

	// Check whether order is ready for delivery
	this.checkOrder(orderId);
    }

    private void scheduleCourier(UUID orderId) {
	long travelSeconds = courierMinSeconds + (long) Math.floor(
		Math.random() * ( courierMaxSeconds + 1 - courierMinSeconds ));
	jobScheduler.schedule(
		() -> courierService.pickupOrder(orderId),
		LocalDateTime.now().plusSeconds(travelSeconds));
    }

    private long calculateAveragePrepareTime(OrderDetail order) {
	long itemCount = 0L;
	long orderPrepareMillis = 0L;
	for (ItemDetail item : order.getItemList()) {
	    long itemPrepareMillis = getItemPrepareMillis(item.getItemId()) * item.getQuantity();
	    itemCount += item.getQuantity();
	    orderPrepareMillis += itemPrepareMillis;
	}
	if (orderPrepareMillis > 0L) {
	    return (orderPrepareMillis / (
		    itemCount < workerCount ? itemCount : workerCount));
	}
	return 1L;
    }

    private void schedulePrepareOrder(OrderDetail order, Long prepareTime) {
	UUID orderId = order.getOrderId();
	for (ItemDetail item : order.getItemList()) {
	    UUID itemId = item.getItemId();
	    long itemScheduleSeconds = (
		prepareTime - getItemPrepareMillis(item.getItemId())) / 1000L;
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

package org.bxo.ordersystem.service.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.CourierService;
import org.bxo.ordersystem.service.ItemService;

@Service
@ConditionalOnProperty(
	name="ordersystem.courier.strategy", havingValue="matched")
public class MatchedCourierServiceImpl implements CourierService {

    private static AtomicLong orderCount = new AtomicLong(0L);
    private static AtomicLong orderWait = new AtomicLong(0L);
    private static AtomicLong courierWait = new AtomicLong(0L);

    private static ConcurrentHashMap<UUID, String> itemCache = new ConcurrentHashMap<>();

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ItemService itemService;

    @Override
    public void pickupOrder(UUID orderId) {
	System.out.printf("MatchedCourierSvc: Courier %s arrived for pickup%n", orderId);
	OrderDetail order = orderRepo.courierArrived(orderId);
	if (null == order) {
	    System.err.printf("MatchedCourierSvc: pickupOrder: Missing order %s for pickup%n", orderId);
	    return;
	}
	if (order.isReady()) {
	    deliverOrder(order);
	}
    }

    @Override
    public void wait(UUID courierId, Long arrivalMillis) {
	// Not used for MatchedCourierService
    }

    @Override
    public void readyOrder(UUID orderId) {
	OrderDetail order = orderRepo.readyOrder(orderId);
	if (null == order) {
	    System.err.printf("MatchedCourierSvc: readyOrder: Order %s not yet placed%n", orderId);
	    return;
	}
	System.out.printf("MatchedCourierSvc: readyOrder: Marked order %s ready for pickup%n", orderId);
	if (order.hasCourier()) {
	    deliverOrder(order);
	}
    }

    private void deliverOrder(OrderDetail order) {
	if (null == order) {
	    System.err.printf("MatchedCourierSvc: deliverOrder: Missing order for pickup%n");
	    return;
	}
	UUID orderId = order.getOrderId();
	if (!order.isPlacedOrder()) {
	    System.err.printf("MatchedCourierSvc: deliverOrder: Order %s not yet submitted%n", orderId);
	    return;
	}
	if (!order.isReady()) {
	    System.err.printf("MatchedCourierSvc: deliverOrder: Order %s not yet ready%n", orderId);
	    return;
	}
	if (!order.hasCourier()) {
	    System.err.printf("MatchedCourierSvc: deliverOrder: Courier %s not yet arrived%n", orderId);
	    return;
	}
	order = orderRepo.removeOrder(orderId);
	if (null == order) {
	    System.err.printf("MatchedCourierSvc: deliverOrder: Duplicate pickup for order %s%n", orderId);
	    return;
	}
	if (order.getItemList().size() != 1) {
	    System.out.printf("MatchedCourierSvc: Courier %s delivered order %s%n", orderId, orderId);
	    for (ItemDetail item : order.getItemList()) {
		String itemName = getItemName(item.getItemId());
		long itemCount = item.getQuantity();
		System.out.printf("MatchedCourierSvc: Courier %s delivered item %s count %d%n", orderId, itemName, itemCount);
	    }
	} else {
	    ItemDetail item = order.getItemList().get(0);
	    System.out.printf(
		"MatchedCourierSvc: Courier %s delivered order %s count %d%n",
		orderId,
		getItemName(item.getItemId()),
		item.getQuantity());
	}

	long pickupMillis = System.currentTimeMillis();
	long orderReadyMillis = order.getReadyOrderMillis();
	long courierArrivalMillis = order.getCourierArrivalMillis();

	// Average food wait time (milliseconds) between order ready and pickup
	long myOrderCount = orderCount.addAndGet(1L);
	long totalOrderWait = orderWait.addAndGet(pickupMillis - orderReadyMillis);
	System.out.printf("%n%nAverage food wait time : %3.0f millis%n",
			  (totalOrderWait * 1.0 / myOrderCount));

	// Average courier wait time (milliseconds) between arrival and order pickup
	long totalCourierWait = courierWait.addAndGet(pickupMillis - courierArrivalMillis);
	System.out.printf("Average courier wait time : %3.0f millis%n%n%n",
			  (totalCourierWait * 1.0 / myOrderCount));

    }

    private String getItemName(UUID itemId) {
	if (!itemCache.containsKey(itemId)) {
	    itemCache.putIfAbsent(itemId, itemService.getItem(itemId).getName());
	}
	if (itemCache.containsKey(itemId)) {
	    return itemCache.get(itemId);
	}
	return itemId.toString();
    }

}

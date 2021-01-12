package org.bxo.ordersystem.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.CourierService;
import org.bxo.ordersystem.service.ItemService;

@Service
@ConditionalOnProperty(
	name="ordersystem.courier.strategy", havingValue="fifo")
public class FifoCourierServiceImpl implements CourierService {

    private static AtomicLong orderCount = new AtomicLong(0L);
    private static AtomicLong orderWait = new AtomicLong(0L);
    private static AtomicLong courierWait = new AtomicLong(0L);

    private static ConcurrentLinkedQueue<UUID> orderQ = new ConcurrentLinkedQueue<>();

    private static ConcurrentHashMap<UUID, String> itemCache = new ConcurrentHashMap<>();

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ItemService itemService;

    @Override
    public void readyOrder(UUID orderId) {
	OrderDetail order = orderRepo.readyOrder(orderId);
	if (null == order) {
	    System.err.printf("FifoCourierSvc: readyOrder: Order %s not yet placed%n", orderId);
	    return;
	}
	orderQ.offer(orderId);
    }

    @Override
    public void pickupOrder(UUID courierId) {
	System.out.printf("FifoCourierSvc: Courier %s arrived for pickup%n", courierId);
	long arrivalMillis = System.currentTimeMillis();
	wait(courierId, arrivalMillis);
    }

    @Override
    public void wait(UUID courierId, Long arrivalMillis) {
	UUID orderId = null;
	OrderDetail order = null;
	while (null == order) {
	    orderId = orderQ.poll();
	    if (null == orderId) {
		jobScheduler.schedule(
		   () -> this.wait(courierId, arrivalMillis),
		   LocalDateTime.now().plusSeconds(1));
		return;
	    }
	    order = orderRepo.getOrder(orderId);
	    if (null == order) {
		System.err.printf("FifoCourierSvc: pickupOrder: Missing order %s for pickup%n", orderId);
	    }
	    if (null != order && !order.isPlacedOrder()) {
		System.err.printf("FifoCourierSvc: deliverOrder: Order %s not yet submitted%n", orderId);
		order = null;
	    }
	    if (null != order && !order.isReady()) {
		System.err.printf("FifoCourierSvc: deliverOrder: Order %s not yet ready%n", orderId);
		order = null;
	    }
	    if (null != order) {
		order = orderRepo.removeOrder(orderId);
		if (null == order) {
		    System.err.printf("FifoCourierSvc: deliverOrder: Duplicate pickup for order %s%n", orderId);
		}
	    }
	}
	if (order.getItemList().size() != 1) {
	    System.out.printf("FifoCourierSvc: Courier %s delivered order %s%n", courierId, orderId);
	    for (ItemDetail item : order.getItemList()) {
		String itemName = getItemName(item.getItemId());
		long itemCount = item.getQuantity();
		System.out.printf("FifoCourierSvc: Courier %s delivered item %s count %d%n", orderId, itemName, itemCount);
	    }
	} else {
	    ItemDetail item = order.getItemList().get(0);
	    System.out.printf(
		"FifoCourierSvc: Courier %s delivered order %s %s count %d%n",
		courierId,
		orderId,
		getItemName(item.getItemId()),
		item.getQuantity());
	}

	long pickupMillis = System.currentTimeMillis();
	long orderReadyMillis = order.getReadyOrderMillis();

	// Average food wait time (milliseconds) between order ready and pickup
	long myOrderCount = orderCount.addAndGet(1L);
	long totalOrderWait = orderWait.addAndGet(pickupMillis - orderReadyMillis);
	System.out.printf("%n%nAverage food wait time : %3.0f millis%n",
			  (totalOrderWait * 1.0 / myOrderCount));

	// Average courier wait time (milliseconds) between arrival and order pickup
	long totalCourierWait = courierWait.addAndGet(pickupMillis - arrivalMillis);
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

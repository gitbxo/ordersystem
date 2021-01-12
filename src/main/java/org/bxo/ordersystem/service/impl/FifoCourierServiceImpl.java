package org.bxo.ordersystem.service.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.CourierService;

@Service
@ConditionalOnProperty(
	name="ordersystem.courier.strategy", havingValue="fifo")
public class FifoCourierServiceImpl implements CourierService {

    private static AtomicLong orderCount = new AtomicLong(0L);
    private static AtomicLong orderWait = new AtomicLong(0L);
    private static AtomicLong courierWait = new AtomicLong(0L);

    private static ConcurrentLinkedQueue<UUID> orderQ = new ConcurrentLinkedQueue<>();

    @Autowired
    private OrderRepository orderRepo;

    @Override
    public void readyOrder(UUID orderId) {
	OrderDetail order = orderRepo.readyOrder(orderId);
	if (null == order) {
	    System.err.printf("FifoCourierSvc: readyOrder: Order %s not yet placed%n", orderId);
	    return;
	}
	orderQ.add(orderId);
    }

    @Override
    public void pickupOrder(UUID courierId) {
	System.out.printf("FifoCourierSvc: Courier %s arrived for pickup%n", courierId);
	long courierArrivalMillis = System.currentTimeMillis();

	UUID orderId = null;
	OrderDetail order = null;
	while (null == order) {
	    orderId = orderQ.poll();
	    if (null == orderId) {
		try {
		    Thread.sleep(1000);  // sleep 1 second
		} catch (InterruptedException e) {
		}
		continue;
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
	    if (null != order && null == orderRepo.removeOrder(orderId)) {
		System.err.printf("FifoCourierSvc: deliverOrder: Duplicate pickup for order %s%n", orderId);
		order = null;
	    }
	}
	System.out.printf("FifoCourierSvc: Courier %s delivered order %s%n", courierId, orderId);

	long pickupMillis = System.currentTimeMillis();
	long orderReadyMillis = order.getReadyOrderMillis();

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

}

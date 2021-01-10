package org.bxo.ordersystem.service.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	System.out.printf("Courier %s arrived for pickup%n", courierId);

	UUID orderId = null;
	while (true) {
	    orderId = orderQ.poll();
	    if (null == orderId) {
		try {
		    Thread.sleep(1000);  // sleep 1 second
		} catch (InterruptedException e) {
		}
		continue;
	    }
	    OrderDetail order = orderRepo.getOrder(orderId);
	    if (null == order) {
		System.err.printf("FifoCourierSvc: pickupOrder: Missing order %s for pickup%n", orderId);
		continue;
	    }
	    if (!order.isPlacedOrder()) {
		System.err.printf("FifoCourierSvc: deliverOrder: Order %s not yet submitted%n", orderId);
		continue;
	    }
	    if (!order.isReady()) {
		System.err.printf("FifoCourierSvc: deliverOrder: Order %s not yet ready%n", orderId);
		continue;
	    }
	    if (null != orderRepo.removeOrder(orderId)) {
		System.err.printf("FifoCourierSvc: deliverOrder: Duplicate pickup for order %s%n", orderId);
		continue;
	    }
	    break;
	}
	System.out.printf("FifoCourierSvc: Courier %s delivered order %s%n", courierId, orderId);
    }

}

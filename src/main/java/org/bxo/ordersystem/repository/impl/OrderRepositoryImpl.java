package org.bxo.ordersystem.repository.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;

@Service
public class OrderRepositoryImpl implements OrderRepository {

    private static ConcurrentHashMap<UUID, OrderDetail> orderMap = new ConcurrentHashMap<>();

    @Override
    public OrderDetail createOrder(UUID orderId) {
	if (!orderMap.containsKey(orderId)) {
	    orderMap.putIfAbsent(orderId, new OrderDetail(orderId, null));
	}
	return getOrder(orderId);
    }

    @Override
    public OrderDetail getOrder(UUID orderId) {
	if (!orderMap.containsKey(orderId)) {
	    return null;
	}
	return orderMap.get(orderId);
    }

    @Override
    public OrderDetail placeOrder(UUID orderId) {
	OrderDetail order = getOrder(orderId);
	if (null != order)
	    order.placeOrder();
	return order;
    }

    @Override
    public OrderDetail readyOrder(UUID orderId) {
	OrderDetail order = getOrder(orderId);
	if (null != order) {
	    order.readyOrder();
	}
	return order;
    }

    @Override
    public OrderDetail courierArrived(UUID orderId) {
	OrderDetail order = getOrder(orderId);
	if (null != order) {
	    order.courierArrived();
	}
	return order;
    }

    @Override
    public OrderDetail removeOrder(UUID orderId) {
	if (!orderMap.containsKey(orderId)) {
	    System.err.printf("removeOrder: Missing order %s%n", orderId);
	    return null;
	}
	return orderMap.remove(orderId);
    }

}

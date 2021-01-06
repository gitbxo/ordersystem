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
    public OrderDetail removeOrder(UUID orderId) {
	if (orderMap.containsKey(orderId)) {
	    return orderMap.remove(orderId);
	}
	return null;
    }

}

package org.bxo.ordersystem.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.api.model.OrderItem;
import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.OrderService;
import org.bxo.ordersystem.service.TaskService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private JobScheduler jobScheduler;

    private static ConcurrentHashMap<UUID, OrderDetail> orderMap = new ConcurrentHashMap<>();

    @Override
    public OrderInfo getOrder(UUID orderId) {
	OrderInfo orderInfo = null;
	OrderDetail detail = orderMap.get(orderId);
	if (null != detail) {
	    List<OrderItem> itemList = new ArrayList<>();
	    if (null != detail.getItemList()) {
		for (ItemDetail item : detail.getItemList()) {
		    itemList.add(new OrderItem(
			item.getItemId(), item.getQuantity()));
		}
	    }
	    orderInfo = new OrderInfo(orderId, itemList);
	}
	return orderInfo;
    }

    @Override
    public OrderInfo createOrder(UUID orderId) {
	if (!orderMap.containsKey(orderId)) {
	    List<OrderItem> itemList = new ArrayList<>();
	    orderMap.putIfAbsent(orderId, new OrderDetail(orderId, itemList));
	}
	return getOrder(orderId);
    }

    @Override
    public OrderInfo submitOrder(UUID orderId) {
	OrderInfo orderInfo = getOrder(orderId);
	if (orderMap.containsKey(orderId)) {
	    orderMap.remove(orderId);
	    jobScheduler.enqueue(() -> taskService.acceptOrder(orderInfo));
	}
	return orderInfo;
    }

    @Override
    public void deleteOrder(UUID orderId) {
	if (orderMap.containsKey(orderId)) {
	    orderMap.remove(orderId);
	}
	return;
    }

    @Override
    public OrderInfo addItem(UUID orderId, UUID itemId, Long quantity) {
	OrderDetail detail = orderMap.get(orderId);
	if (null != detail && null != itemService.getItem(itemId)) {
	    detail.addItem(itemId, quantity);
	}
	return getOrder(orderId);
    }

    @Override
    public OrderInfo deleteItem(UUID orderId, UUID itemId) {
	OrderDetail detail = orderMap.get(orderId);
	if (null != detail) {
	    detail.deleteItem(itemId);
	}
	return getOrder(orderId);
    }

}

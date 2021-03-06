package org.bxo.ordersystem.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.api.model.OrderItem;
import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
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

    @Autowired
    private OrderRepository orderRepo;

    @Override
    public OrderInfo getOrder(UUID orderId) {
	OrderInfo orderInfo = null;
	OrderDetail detail = orderRepo.getOrder(orderId);
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
	orderRepo.createOrder(orderId);
	return getOrder(orderId);
    }

    @Override
    public OrderInfo submitOrder(UUID orderId) {
	OrderInfo orderInfo = getOrder(orderId);
	if (null != orderInfo && orderInfo.getOrderId().equals(orderId)) {
	    jobScheduler.enqueue(() -> taskService.acceptOrder(orderId));
	}
	return orderInfo;
    }

    @Override
    public void deleteOrder(UUID orderId) {
	orderRepo.removeOrder(orderId);
    }

    @Override
    public OrderInfo addItem(UUID orderId, UUID itemId, Long quantity) {
	OrderDetail detail = orderRepo.getOrder(orderId);
	if (null != detail && null != itemService.getItem(itemId)) {
	    detail.addItem(itemId, quantity);
	}
	return getOrder(orderId);
    }

    @Override
    public OrderInfo deleteItem(UUID orderId, UUID itemId) {
	OrderDetail detail = orderRepo.getOrder(orderId);
	if (null != detail) {
	    detail.deleteItem(itemId);
	}
	return getOrder(orderId);
    }

}

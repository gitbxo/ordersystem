package org.bxo.ordersystem.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.api.model.OrderItem;

public interface OrderService {

    public OrderInfo getOrder(UUID orderId);

    public OrderInfo createOrder(UUID orderId);

    public OrderInfo submitOrder(UUID orderId);

    public void deleteOrder(UUID orderId);

    public OrderInfo addItem(UUID orderId, UUID itemId, Long quantity);

    public OrderInfo deleteItem(UUID orderId, UUID itemId);

}

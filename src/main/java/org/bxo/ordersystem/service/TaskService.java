package org.bxo.ordersystem.service;

import java.util.UUID;

import org.bxo.ordersystem.api.model.OrderInfo;

public interface TaskService {

    public void acceptOrder(OrderInfo order);

    public void prepareItem(UUID orderId, UUID itemId);

    public void deliverOrder(UUID orderId);

}

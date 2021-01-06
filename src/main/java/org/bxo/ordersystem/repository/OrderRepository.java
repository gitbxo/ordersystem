package org.bxo.ordersystem.repository;

import java.util.UUID;

import org.bxo.ordersystem.model.OrderDetail;

public interface OrderRepository {

    public OrderDetail createOrder(UUID orderId);

    public OrderDetail getOrder(UUID orderId);

    public OrderDetail removeOrder(UUID orderId);

}

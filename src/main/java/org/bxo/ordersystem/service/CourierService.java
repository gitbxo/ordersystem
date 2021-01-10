package org.bxo.ordersystem.service;

import java.util.UUID;

public interface CourierService {

    public void pickupOrder(UUID orderId);

    public void readyOrder(UUID orderId);

}

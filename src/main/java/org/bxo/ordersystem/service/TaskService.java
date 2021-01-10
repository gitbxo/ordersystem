package org.bxo.ordersystem.service;

import java.util.UUID;

public interface TaskService {

    public void acceptOrder(UUID orderId);

    public void prepareItem(UUID orderId, UUID itemId);

}

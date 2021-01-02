package org.bxo.ordersystem.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.bxo.ordersystem.service.OrderService;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void createOrder_saves_order() {
	UUID orderId = UUID.randomUUID();
        orderService.createOrder(orderId);
        assertNotNull(orderService.getOrder(orderId));
    }

}

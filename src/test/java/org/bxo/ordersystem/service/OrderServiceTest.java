package org.bxo.ordersystem.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.service.OrderService;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void createOrder_saves_order() {
	UUID orderId = UUID.randomUUID();
        orderService.createOrder(orderId);
	OrderInfo orderInfo = orderService.getOrder(orderId);
	assertThat(orderInfo, notNullValue());
	assertThat(orderInfo.getOrderId(), is(orderId));
    }

}

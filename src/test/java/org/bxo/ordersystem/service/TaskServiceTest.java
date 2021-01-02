package org.bxo.ordersystem.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.api.model.OrderItem;
import org.bxo.ordersystem.service.TaskService;

@SpringBootTest
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void acceptOrder_savesOrder() {
	UUID orderId = UUID.randomUUID();
	List<OrderItem> itemList = new ArrayList<>();
	OrderInfo orderInfo = new OrderInfo(orderId, itemList);
        taskService.acceptOrder(orderInfo);
    }

}

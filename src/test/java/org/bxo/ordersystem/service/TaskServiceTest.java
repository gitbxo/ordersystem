package org.bxo.ordersystem.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.TaskService;

@SpringBootTest
public class TaskServiceTest {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private TaskService taskService;

    @Test
    public void deliverOrder_deletesOrder() {
	UUID orderId = UUID.randomUUID();
	UUID itemId = UUID.randomUUID();
	OrderDetail order = orderRepo.createOrder(orderId);
	order.addItem(itemId, 5L);

        taskService.deliverOrder(orderId);
	assertThat(orderRepo.getOrder(orderId), nullValue());
    }

}

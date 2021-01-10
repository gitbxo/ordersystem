package org.bxo.ordersystem.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jobrunr.autoconfigure.JobRunrAutoConfiguration;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.OrderService;
import org.bxo.ordersystem.service.TaskService;

@SpringBootTest
public class OrderServiceTest {

    @MockBean
    private JobRunrAutoConfiguration jobConfig;

    @MockBean
    private JobMapper jobMapper;

    @MockBean
    private JobScheduler jobScheduler;

    @MockBean
    private ItemService itemService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private OrderRepository orderRepo;

    @Autowired
    private OrderService orderService;

    @Test
    public void getOrder_calls_repo() {
	UUID orderId = UUID.randomUUID();
	UUID itemId = UUID.randomUUID();
	OrderDetail order = new OrderDetail(orderId, null);
	order.addItem(itemId, 5L);
	Mockito.when(orderRepo.getOrder(orderId)).thenReturn(order);

	OrderInfo orderInfo = orderService.getOrder(orderId);
	assertThat(orderInfo.getOrderId(), is(orderId));
	assertThat(orderInfo.getItemList(), notNullValue());
	assertThat(orderInfo.getItemList().size(), is(1));
	assertThat(orderInfo.getItemList().get(0).getItemId(), is(itemId));
	assertThat(orderInfo.getItemList().get(0).getQuantity(), is(5L));
    }

    @Test
    public void createOrder_saves_order() {
	UUID orderId = UUID.randomUUID();
	OrderDetail order = new OrderDetail(orderId, null);
	Mockito.when(orderRepo.createOrder(orderId)).thenReturn(order);
	Mockito.when(orderRepo.getOrder(orderId)).thenReturn(order);

	orderService.createOrder(orderId);
	Mockito.verify(orderRepo).createOrder(orderId);
	Mockito.verify(orderRepo).getOrder(orderId);
    }

    @Test
    public void submitOrder_queues_job() {
	UUID orderId = UUID.randomUUID();
	OrderDetail order = new OrderDetail(orderId, null);
	Mockito.when(orderRepo.getOrder(orderId)).thenReturn(order);
	Mockito.when(jobScheduler.enqueue(any(JobLambda.class))).thenReturn(null);

	orderService.submitOrder(orderId);
	Mockito.verify(orderRepo).getOrder(orderId);
	Mockito.verify(jobScheduler).enqueue(any(JobLambda.class));
    }

    @Test
    public void deleteOrder_calls_removeOrder() {
	UUID orderId = UUID.randomUUID();
	Mockito.when(orderRepo.removeOrder(orderId)).thenReturn(null);
	orderService.deleteOrder(orderId);
	Mockito.verify(orderRepo).removeOrder(orderId);
    }

}

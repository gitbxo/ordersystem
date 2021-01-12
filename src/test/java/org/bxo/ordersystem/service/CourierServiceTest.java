package org.bxo.ordersystem.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.jobrunr.autoconfigure.JobRunrAutoConfiguration;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.CourierService;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.OrderService;

@SpringBootTest
public class CourierServiceTest {

    @MockBean
    private JobRunrAutoConfiguration jobConfig;

    @MockBean
    private JobMapper jobMapper;

    @MockBean
    private JobScheduler jobScheduler;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CourierService courierService;

    @Test
    public void pickupOrder_notReady_keeps_order() {
	UUID orderId = UUID.randomUUID();
	UUID itemId = UUID.randomUUID();

	OrderDetail order = orderRepo.createOrder(orderId);
	order.addItem(itemId, 5L);
	orderRepo.placeOrder(orderId);

	courierService.pickupOrder(orderId);
	assertThat(orderRepo.getOrder(orderId), isA(OrderDetail.class));
	assertThat(orderRepo.getOrder(orderId).getOrderId(), is(orderId));
    }

    @Test
    public void pickupOrder_isReady_deletes_order() {
	UUID orderId = UUID.randomUUID();
	UUID itemId = UUID.randomUUID();

	OrderDetail order = orderRepo.createOrder(orderId);
	order.addItem(itemId, 5L);
	orderRepo.placeOrder(orderId);
	courierService.readyOrder(orderId);

	itemService.createItem(itemId, "item name", 7L, 9L);
	courierService.pickupOrder(orderId);
	assertThat(orderRepo.getOrder(orderId), nullValue());
    }

    @Test
    public void readyOrder_noCourier_keeps_order() {
	UUID orderId = UUID.randomUUID();
	UUID itemId = UUID.randomUUID();

	OrderDetail order = orderRepo.createOrder(orderId);
	order.addItem(itemId, 5L);
	orderRepo.placeOrder(orderId);

	courierService.readyOrder(orderId);
	assertThat(orderRepo.getOrder(orderId), isA(OrderDetail.class));
	assertThat(orderRepo.getOrder(orderId).getOrderId(), is(orderId));
    }

    @Test
    public void readyOrder_hasCourier_deletes_order() {
	UUID orderId = UUID.randomUUID();
	UUID itemId = UUID.randomUUID();

	OrderDetail order = orderRepo.createOrder(orderId);
	order.addItem(itemId, 5L);
	orderRepo.placeOrder(orderId);
	courierService.pickupOrder(orderId);

	itemService.createItem(itemId, "item name", 7L, 9L);
	courierService.readyOrder(orderId);
	assertThat(orderRepo.getOrder(orderId), nullValue());
    }

}

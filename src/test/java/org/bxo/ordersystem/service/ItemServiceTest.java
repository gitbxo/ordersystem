package org.bxo.ordersystem.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

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

import org.bxo.ordersystem.api.model.ItemInfo;
import org.bxo.ordersystem.repository.OrderRepository;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.OrderService;
import org.bxo.ordersystem.service.TaskService;

@SpringBootTest
public class ItemServiceTest {

    @MockBean
    private JobRunrAutoConfiguration jobConfig;

    @MockBean
    private JobMapper jobMapper;

    @MockBean
    private JobScheduler jobScheduler;

    @MockBean
    private TaskService taskService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepo;

    @Autowired
    private ItemService itemService;

    @Test
    public void getItem_returns_created_item() {
	// This tests both getItem() and createItem();
	UUID itemId = UUID.randomUUID();
	String name = "item name";

	ItemInfo item = itemService.createItem(itemId, name, 7L, 9L);
	assertThat(itemService.getItem(itemId), is(item));
	assertThat(item.getItemId(), is(itemId));
	assertThat(item.getName(), is(name));
	assertThat(item.getPrepareTimeMillis(), is(7L));
	assertThat(item.getExpiryTimeMillis(), is(9L));
    }

    @Test
    public void deleteItem_removes_item() {
	UUID itemId = UUID.randomUUID();
	String name = "item name";

	assertThat(itemService.getItem(itemId), nullValue());
	ItemInfo item = itemService.createItem(itemId, name, 7L, 9L);
	assertThat(itemService.getItem(itemId), is(item));

	itemService.deleteItem(itemId);
	assertThat(itemService.getItem(itemId), nullValue());
    }

}

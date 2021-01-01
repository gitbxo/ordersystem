package org.bxo.ordersystem.service.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import org.bxo.ordersystem.api.model.ItemInfo;
import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.api.model.OrderItem;
import org.bxo.ordersystem.model.ItemDetail;
import org.bxo.ordersystem.model.OrderDetail;
import org.bxo.ordersystem.service.ItemService;
import org.bxo.ordersystem.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    // @Autowired
    // private AsyncTaskExecutor workerExecutor;

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private ItemService itemService;

    @Value("${org.jobrunr.background-job-server.worker_count:10}")
    private Long workerCount;

    private static ConcurrentHashMap<UUID, OrderDetail> orderMap = new ConcurrentHashMap<>();

    @Override
    public void acceptOrder(OrderInfo order) {
	UUID orderId = order.getOrderId();
	if (orderMap.containsKey(orderId)) {
	    System.err.printf("Duplicate order %s%n", orderId);
	    return;
	}
	System.out.printf("Accept order %s%n", orderId);
	// workerExecutor.submit(createTask(1));
	orderMap.putIfAbsent(orderId, new OrderDetail(orderId, order.getItemList()));
	for (OrderItem item : order.getItemList()) {
	    UUID itemId = item.getItemId();
	    for (int i=0; i < item.getQuantity(); i++) {
		jobScheduler.enqueue(() -> this.prepareItem(orderId, itemId));
	    }
	}
    }

    @Override
    public void prepareItem(UUID orderId, UUID itemId) {
	if (!orderMap.containsKey(orderId)) {
	    System.err.printf("Missing order %s to prepare item %s%n", orderId, itemId);
	    return;
	}
	OrderDetail order = orderMap.get(orderId);
	ItemDetail item = order.getItemDetail(itemId);
	if (null == item) {
	    System.err.printf("Order %s missing item %s to prepare%n", orderId, itemId);
	    return;
	}
	ItemInfo info = itemService.getItem(itemId);
	if (info != null) {
	    long prepareSeconds = info.getPrepareTimeSeconds();
	    try {
		Thread.sleep(prepareSeconds * 1000);
	    } catch (InterruptedException e) {
	    }
	}
	long newQty = item.addPreparedQty(1);
	System.out.printf(
		"Order %s prepared item %s qty %d of %d%n", orderId, itemId,
		newQty, item.getQuantity());
	// workerExecutor.submit(createTask(2));

	if (newQty >= item.getQuantity()) {
	    boolean allPrepared = true;
	    for (ItemDetail i : order.getItemList()) {
		if (i.getPreparedQty() < i.getQuantity()) {
		    allPrepared = false;
		    break;
		}
	    }
	    if (allPrepared) {
		jobScheduler.enqueue(() -> this.deliverOrder(orderId));
	    }
	}
    }

    @Override
    public void deliverOrder(UUID orderId) {
	OrderDetail order = orderMap.remove(orderId);
	if (null == order) {
	    System.err.printf("Missing order %s for delivery%n", orderId);
	    return;
	}
	System.out.printf("Deliver order %s%n", orderId);
	// workerExecutor.submit(createTask(3));
    }

    private Callable<String> createTask(int i) {
	return () -> {
	    System.out.printf("running task %d. Thread: %s%n",
			      i,
			      Thread.currentThread().getName());
	    return String.format("Task finished %d", i);
	};
    }

}

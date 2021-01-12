package org.bxo.ordersystem.model;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.JMRuntimeException;

import org.bxo.ordersystem.api.model.OrderItem;

/*
 * This class contains an order that will be processed.
 * This is used both for the shopping cart (when placedOrder = false)
 * and when the order is being processed (when placedOrder = true)
 *
 * TODO: This is a proof of concept.
 *       Break this up into separate classes when saving to the database.
 *       The item list will need to be saved in separate table.
 */

public class OrderDetail {

    private AtomicLong placedOrderMillis = new AtomicLong(0L);
    private AtomicLong readyOrderMillis = new AtomicLong(0L);
    private AtomicLong courierArrivalMillis = new AtomicLong(0L);
    private final UUID orderId;
    private final ConcurrentHashMap<UUID, ItemDetail> itemMap;

    public OrderDetail(UUID orderId, List<OrderItem> itemList) {
	this.orderId = orderId;
	this.itemMap = new ConcurrentHashMap<>();
	if (null != itemList) {
	    for (OrderItem item : itemList) {
		itemMap.putIfAbsent(item.getItemId(), new ItemDetail(
			item.getItemId(), item.getQuantity()));
	    }
	}
    }

    public UUID getOrderId() {
	return orderId;
    }

    public List<ItemDetail> getItemList() {
	List<ItemDetail> itemList = new ArrayList<>();
	for (Map.Entry<UUID, ItemDetail> item : itemMap.entrySet()) {
	    itemList.add(item.getValue());
	}
	return itemList;
    }

    public void addItem(UUID itemId, Long quantity) {
	if (this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Cannot modify a placed order : " + getOrderId().toString());
	}
	if (null == quantity || quantity <= 0) {
	    return;
	}
	if (quantity > 0) {
	    itemMap.putIfAbsent(itemId, new ItemDetail(itemId, 0L));
	}
	if (itemMap.containsKey(itemId)) {
	    ItemDetail item = itemMap.get(itemId);
	    long qty = item.addQuantity(quantity);
	    if (qty <= 0) {
		itemMap.remove(itemId);
	    }
	}
    }

    public ItemDetail getItemDetail(UUID itemId) {
	if (null != itemMap && itemMap.containsKey(itemId)) {
	    return itemMap.get(itemId);
	}
	return null;
    }

    public void deleteItem(UUID itemId) {
	if (this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Cannot modify a placed order : " + getOrderId().toString());
	}
	if (null != itemMap && itemMap.containsKey(itemId)) {
	    itemMap.remove(itemId);
	}
    }

    public void placeOrder() {
	if (!placedOrderMillis.compareAndSet(0L, System.currentTimeMillis())) {
	    throw new JMRuntimeException(
		"OrderDetail: Cannot modify a placed order : " + getOrderId().toString());
	}
    }

    public boolean isPlacedOrder() {
	return (this.placedOrderMillis.get() > 0L);
    }

    public long getPlacedOrderMillis() {
	return this.placedOrderMillis.get();
    }

    public void readyOrder() {
	if (!this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Must place order before marking ready");
	}
	if (!readyOrderMillis.compareAndSet(0L, System.currentTimeMillis())) {
	    throw new JMRuntimeException(
		"OrderDetail: Order previously marked ready : " + getOrderId().toString());
	}
    }

    public boolean isReady() {
	return (this.readyOrderMillis.get() > 0L);
    }

    public long getReadyOrderMillis() {
	return this.readyOrderMillis.get();
    }

    public void courierArrived() {
	if (!this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Must place order before courier arrives");
	}
	if (!courierArrivalMillis.compareAndSet(0L, System.currentTimeMillis())) {
	    throw new JMRuntimeException(
		"OrderDetail: Courier already arrived : " + getOrderId().toString());
	}
    }

    public boolean hasCourier() {
	return (this.courierArrivalMillis.get() > 0L);
    }

    public long getCourierArrivalMillis() {
	return this.courierArrivalMillis.get();
    }

}

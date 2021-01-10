package org.bxo.ordersystem.model;

import java.time.LocalDateTime;
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

    private LocalDateTime placedOrderTime = null;
    private LocalDateTime readyOrderTime = null;
    private LocalDateTime courierArrivalTime = null;
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
	if (this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Cannot modify a placed order : " + getOrderId().toString());
	}
	this.placedOrderTime = LocalDateTime.now();
    }

    public boolean isPlacedOrder() {
	return (null != this.placedOrderTime);
    }

    public LocalDateTime getPlacedOrderTime() {
	return this.placedOrderTime;
    }

    public void readyOrder() {
	if (!this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Must place order before marking ready");
	}
	if (this.isReady()) {
	    throw new JMRuntimeException(
		"OrderDetail: Order previously marked ready : " + getOrderId().toString());
	}
	this.readyOrderTime = LocalDateTime.now();
    }

    public boolean isReady() {
	return (null != this.readyOrderTime);
    }

    public LocalDateTime getReadyOrderTime() {
	return this.readyOrderTime;
    }

    public void courierArrived() {
	if (!this.isPlacedOrder()) {
	    throw new JMRuntimeException(
		"OrderDetail: Must place order before courier arrives");
	}
	if (this.hasCourier()) {
	    throw new JMRuntimeException(
		"OrderDetail: Courier already arrived : " + getOrderId().toString());
	}
	this.courierArrivalTime = LocalDateTime.now();
    }

    public boolean hasCourier() {
	return (null != this.courierArrivalTime);
    }

    public LocalDateTime getCourierArrivalTime() {
	return this.courierArrivalTime;
    }

}

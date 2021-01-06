package org.bxo.ordersystem.model;

import java.util.concurrent.atomic.AtomicBoolean;
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

    private final AtomicBoolean placedOrder;
    private final UUID orderId;
    private final ConcurrentHashMap<UUID, ItemDetail> itemMap;

    public OrderDetail(UUID orderId, List<OrderItem> itemList) {
	this.orderId = orderId;
	this.placedOrder = new AtomicBoolean(false);
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
	if (this.placedOrder.get()) {
	    throw new JMRuntimeException("Cannot modify a placed order");
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
	if (this.placedOrder.get()) {
	    throw new JMRuntimeException("Cannot modify a placed order");
	}
	if (null != itemMap && itemMap.containsKey(itemId)) {
	    itemMap.remove(itemId);
	}
    }

    public void placeOrder() {
	if (this.placedOrder.get()) {
	    throw new JMRuntimeException("Cannot modify a placed order");
	}
	this.placedOrder.set(true);
    }

    public Boolean getPlacedOrder() {
	return this.placedOrder.get();
    }

}

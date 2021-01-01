package org.bxo.ordersystem.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bxo.ordersystem.api.model.OrderItem;

public class OrderDetail {

    private UUID orderId;
    private ConcurrentHashMap<UUID, ItemDetail> itemMap;

    public OrderDetail(UUID orderId, List<OrderItem> itemList) {
	this.orderId = orderId;
	if (null != itemList) {
	    itemMap = new ConcurrentHashMap<>();
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
	if (null != itemMap) {
	    for (Map.Entry<UUID, ItemDetail> item : itemMap.entrySet()) {
		itemList.add(item.getValue());
	    }
	}
	return itemList;
    }

    public void addItem(UUID itemId, Long quantity) {
	if (null == quantity || quantity <= 0) {
	    return;
	}
	if (null == itemMap) {
	    itemMap = new ConcurrentHashMap<>();
	}
	if (itemMap.containsKey(itemId)) {
	    ItemDetail item = itemMap.get(itemId);
	    long qty = item.addQuantity(quantity);
	    if (qty <= 0) {
		itemMap.remove(itemId);
	    }
	} else if (quantity > 0) {
	    itemMap.putIfAbsent(
		itemId, new ItemDetail(itemId, quantity));
	}
    }

    public ItemDetail getItemDetail(UUID itemId) {
	if (null != itemMap && itemMap.containsKey(itemId)) {
	    return itemMap.get(itemId);
	}
	return null;
    }

    public void deleteItem(UUID itemId) {
	if (null != itemMap && itemMap.containsKey(itemId)) {
	    itemMap.remove(itemId);
	}
    }

}

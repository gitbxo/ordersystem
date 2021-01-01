package org.bxo.ordersystem.api.model;

import java.util.UUID;

public class OrderItem {

    private UUID itemId;
    private long quantity;

    public OrderItem() {
	itemId = null;
	quantity = 0;
    }

    public OrderItem(UUID itemId, long quantity) {
	this.itemId = itemId;
	this.quantity = quantity;
    }

    public UUID getItemId() {
	return itemId;
    }

    public void setItemId(UUID itemId) {
	this.itemId = itemId;
    }

    public long getQuantity() {
	return quantity;
    }

    public void setQuantity(long quantity) {
	this.quantity = quantity;
    }

}

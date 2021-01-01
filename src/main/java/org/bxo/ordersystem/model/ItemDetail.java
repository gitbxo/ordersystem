package org.bxo.ordersystem.model;

import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

public class ItemDetail {

    private final UUID itemId;
    private final AtomicLong quantity;
    private final AtomicLong preparedQty;

    public ItemDetail(UUID itemId, long quantity) {
	this(itemId, quantity, 0L);
    }

    public ItemDetail(UUID itemId, long quantity, long preparedQty) {
	this.itemId = itemId;
	this.quantity = new AtomicLong(quantity);
	this.preparedQty = new AtomicLong(preparedQty);
    }

    public UUID getItemId() {
	return itemId;
    }

    public long getQuantity() {
	return quantity.longValue();
    }

    public void setQuantity(long quantity) {
	this.quantity.set(quantity);
    }

    public long addQuantity(long add) {
	return this.quantity.addAndGet(add);
    }

    public long getPreparedQty() {
	return preparedQty.longValue();
    }

    public void setPreparedQty(long preparedQty) {
	this.preparedQty.set(preparedQty);
    }

    public long addPreparedQty(long add) {
	return this.preparedQty.addAndGet(add);
    }

}

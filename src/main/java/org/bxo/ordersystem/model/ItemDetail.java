package org.bxo.ordersystem.model;

import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ItemDetail {

    private final UUID itemId;
    private final AtomicLong quantity;
    private final AtomicLong preparedQty;
    private final List<Long> prepareTimeList;

    public ItemDetail(UUID itemId, long quantity) {
	this(itemId, quantity, 0L);
    }

    public ItemDetail(UUID itemId, long quantity, long preparedQty) {
	this.itemId = itemId;
	this.quantity = new AtomicLong(quantity);
	this.preparedQty = new AtomicLong(preparedQty);
	this.prepareTimeList = Collections.synchronizedList(new ArrayList<Long>());
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
	long now = System.currentTimeMillis();
	for (int i=0; i < add; i++) {
	    prepareTimeList.add(now);
	}
	return this.preparedQty.addAndGet(add);
    }

    public long getExpiredQty(long expiryMillis) {
	long expiredTime = System.currentTimeMillis() - expiryMillis;
	long expiredQty = 0L;
	for (int i=0; i < prepareTimeList.size(); i++) {
	    if (prepareTimeList.get(i) < expiredTime) {
		expiredQty += 1;
	    }
	}
	return expiredQty;
    }

}

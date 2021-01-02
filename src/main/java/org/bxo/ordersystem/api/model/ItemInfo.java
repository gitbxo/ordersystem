package org.bxo.ordersystem.api.model;

import java.util.UUID;

public class ItemInfo {

    private final UUID itemId;
    private final String name;
    private final long prepareTimeMillis;
    private final long expiryTimeMillis;

    public ItemInfo(UUID itemId, String name, long prepareTimeMillis,
		    long expiryTimeMillis) {
	this.itemId = itemId;
	this.name = name;
	this.prepareTimeMillis = prepareTimeMillis;
	this.expiryTimeMillis = expiryTimeMillis;
    }

    public UUID getItemId() {
	return itemId;
    }

    public String getName() {
	return name;
    }

    public long getPrepareTimeMillis() {
	return prepareTimeMillis;
    }

    public long getExpiryTimeMillis() {
	return expiryTimeMillis;
    }

}

package org.bxo.ordersystem.api.model;

import java.util.UUID;

public class ItemInfo {

    private final UUID itemId;
    private final String name;
    private final long prepareTimeSeconds;
    private final long expiryTimeSeconds;

    public ItemInfo(UUID itemId, String name, long prepareTimeSeconds,
		    long expiryTimeSeconds) {
	this.itemId = itemId;
	this.name = name;
	this.prepareTimeSeconds = prepareTimeSeconds;
	this.expiryTimeSeconds = expiryTimeSeconds;
    }

    public UUID getItemId() {
	return itemId;
    }

    public String getName() {
	return name;
    }

    public long getPrepareTimeSeconds() {
	return prepareTimeSeconds;
    }

    public long getExpiryTimeSeconds() {
	return expiryTimeSeconds;
    }

}

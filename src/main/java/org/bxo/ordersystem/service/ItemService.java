package org.bxo.ordersystem.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bxo.ordersystem.api.model.ItemInfo;

public interface ItemService {

    public ItemInfo getItem(UUID itemId);

    public ItemInfo createItem(
		UUID itemId, String name,
		long prepareTimeSeconds, long expiryTimeSeconds);

    public void deleteItem(UUID itemId);

}

package org.bxo.ordersystem.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.bxo.ordersystem.api.model.ItemInfo;
import org.bxo.ordersystem.service.ItemService;

@Service
public class ItemServiceImpl implements ItemService {

    private static ConcurrentHashMap<UUID, ItemInfo> itemMap = new ConcurrentHashMap<>();

    @Override
    public ItemInfo getItem(UUID itemId) {
	ItemInfo itemInfo = null;
	if (itemMap.containsKey(itemId)) {
	    itemInfo = itemMap.get(itemId);
	}
	return itemInfo;
    }

    @Override
    public ItemInfo createItem(
		UUID itemId, String name,
		long prepareTimeSeconds, long expiryTimeSeconds) {
	if (!itemMap.containsKey(itemId)) {
	    itemMap.putIfAbsent(itemId, new ItemInfo(
		itemId, name, prepareTimeSeconds, expiryTimeSeconds));
	}
	return getItem(itemId);
    }

    @Override
    public void deleteItem(UUID itemId) {
	if (itemMap.containsKey(itemId)) {
	    itemMap.remove(itemId);
	}
	return;
    }

}

package org.bxo.ordersystem.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.bxo.ordersystem.api.model.ItemInfo;
import org.bxo.ordersystem.service.ItemService;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping()
    public ItemInfo getItem(@RequestParam(name="itemId", required=true) String itemId) {
	return itemService.getItem(UUID.fromString(itemId));
    }

    @PostMapping()
    public ItemInfo createItem(
		@RequestParam(name="name", required=true) String name,
		@RequestParam(name="prepareSeconds", required=true) long prepareTimeSeconds,
		@RequestParam(name="expirySeconds", required=true) long expiryTimeSeconds) {
	return itemService.createItem(
		UUID.randomUUID(), name,
		prepareTimeSeconds, expiryTimeSeconds);
    }

    @PostMapping("/test")
    public ItemInfo createTestItem(
		@RequestParam(name="itemId", required=true) String itemId,
		@RequestParam(name="name", required=true) String name,
		@RequestParam(name="prepareSeconds", required=true) long prepareTimeSeconds,
		@RequestParam(name="expirySeconds", required=true) long expiryTimeSeconds) {
	return itemService.createItem(
		UUID.fromString(itemId), name,
		prepareTimeSeconds, expiryTimeSeconds);
    }

    @DeleteMapping()
    public void deleteItem(@RequestParam(name="itemId", required=true) String itemId) {
	itemService.deleteItem(UUID.fromString(itemId));
    }

}

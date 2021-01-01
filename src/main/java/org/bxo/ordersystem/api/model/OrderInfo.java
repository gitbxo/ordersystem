package org.bxo.ordersystem.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderInfo {

    private UUID orderId;
    private List<OrderItem> itemList;

    public OrderInfo() {}

    public OrderInfo(UUID orderId, List<OrderItem> itemList) {
	this.orderId = orderId;
	this.itemList = itemList;
    }

    public UUID getOrderId() {
	return orderId;
    }

    public void setOrderId(UUID orderId) {
	this.orderId = orderId;
    }

    public List<OrderItem> getItemList() {
	List<OrderItem> returnList = new ArrayList<>();
	returnList.addAll(itemList);
	return returnList;
    }

    public void setItemList(List<OrderItem> itemList) {
	this.itemList = itemList;
    }

}

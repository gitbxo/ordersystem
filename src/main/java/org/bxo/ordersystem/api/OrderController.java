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

import org.bxo.ordersystem.api.model.OrderInfo;
import org.bxo.ordersystem.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping()
    public OrderInfo getOrder(@RequestParam(name="orderId", required=true) String orderId) {
	return orderService.getOrder(UUID.fromString(orderId));
    }

    @PostMapping()
    public OrderInfo createOrder() {
	return orderService.createOrder(UUID.randomUUID());
    }

    @PostMapping("/test")
    public OrderInfo createTestOrder(@RequestParam(name="orderId", required=true) String orderId) {
	return orderService.createOrder(UUID.fromString(orderId));
    }

    @PutMapping("/submit")
    public OrderInfo submitOrder(@RequestParam(name="orderId", required=true) String orderId) {
	return orderService.submitOrder(UUID.fromString(orderId));
    }

    @DeleteMapping()
    public void deleteOrder(@RequestParam(name="orderId", required=true) String orderId) {
	orderService.deleteOrder(UUID.fromString(orderId));
    }

    @PutMapping("/item")
    public OrderInfo addItem(
	@RequestParam(name="orderId", required=true) String orderId,
	@RequestParam(name="itemId", required=true) String itemId,
	@RequestParam(name="quantity", required=true) Long quantity) {

	return orderService.addItem(UUID.fromString(orderId), UUID.fromString(itemId), quantity);
    }

    @DeleteMapping("/item")
    public OrderInfo deleteItem(
	@RequestParam(name="orderId", required=true) String orderId,
	@RequestParam(name="itemId", required=true) String itemId) {

	return orderService.deleteItem(UUID.fromString(orderId), UUID.fromString(itemId));
    }

}

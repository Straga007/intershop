package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ShopService {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    // Временно используем одну сессию для всего приложения
    private static final String SHARED_SESSION_ID = "SHARED_SESSION";

    public ShopService(ItemService itemService, CartService cartService, OrderService orderService) {
        this.itemService = itemService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    public Mono<List<List<ItemDto>>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        return itemService.getMainItems(search, sort, pageSize, pageNumber);
    }

    public Mono<Void> updateMainItemQuantity(String sessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(SHARED_SESSION_ID, itemId, action);
    }

    public Mono<List<ItemDto>> getCartItems(String sessionId) {
        return cartService.getCartItems(SHARED_SESSION_ID);
    }

    public Mono<Void> updateCartItemQuantity(String sessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(SHARED_SESSION_ID, itemId, action);
    }

    public Mono<ItemDto> getItem(String id) {
        return itemService.getItemById(id);
    }

    public Mono<Void> updateItemQuantity(String sessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(SHARED_SESSION_ID, itemId, action);
    }

    public Mono<String> buy(String sessionId) {
        return orderService.createOrder(SHARED_SESSION_ID);
    }

    public Mono<List<OrderDto>> getOrders(String sessionId) {
        return orderService.getAllOrders();
    }

    public Mono<OrderDto> getOrder(String id) {
        return orderService.getOrderById(id);
    }

    public Mono<Double> getCartTotal(String sessionId) {
        return cartService.getCartTotal(sessionId);
    }

    public Mono<Boolean> isCartEmpty(String sessionId) {
        return cartService.isCartEmpty(sessionId);
    }
}

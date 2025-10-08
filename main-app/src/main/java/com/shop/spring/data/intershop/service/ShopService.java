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

    public Mono<Void> updateMainItemQuantity(String ignoredSessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(SHARED_SESSION_ID, itemId, action);
    }

    public Mono<List<ItemDto>> getCartItems(String ignoredSessionId) {
        return cartService.getCartItems(SHARED_SESSION_ID);
    }

    public Mono<Void> updateCartItemQuantity(String ignoredSessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(SHARED_SESSION_ID, itemId, action);
    }

    public Mono<ItemDto> getItem(String id) {
        return itemService.getItemById(id);
    }

    public Mono<Void> updateItemQuantity(String ignoredSessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(SHARED_SESSION_ID, itemId, action);
    }

    public Mono<String> buy(String ignoredSessionId) {
        return orderService.createOrder(SHARED_SESSION_ID);
    }

    public Mono<List<OrderDto>> getOrders(String ignoredSessionId) {
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
    
    public Mono<Double> checkBalance() {
        System.out.println("Вызов метода checkBalance в ShopService");
        return orderService.checkBalance()
                .doOnNext(balance -> System.out.println("Получен баланс из OrderService: " + balance))
                .doOnError(throwable -> System.out.println("Ошибка в ShopService при получении баланса: " + throwable.getMessage()));
    }
}
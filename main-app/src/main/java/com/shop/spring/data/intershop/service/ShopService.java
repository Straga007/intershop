package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;

    public Mono<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        return catalogService.getMainItems(search, sort, pageSize, pageNumber);
    }

    public Mono<ItemDto> getItem(String id) {
        return catalogService.getItem(id);
    }

    public Mono<List<ItemDto>> getCartItems(String sessionId) {
        return cartService.getCartItems(sessionId);
    }

    public Mono<Double> getCartTotal(String sessionId) {
        return cartService.getCartTotal(sessionId);
    }

    public Mono<Boolean> isCartEmpty(String sessionId) {
        return cartService.isCartEmpty(sessionId);
    }

    public Mono<String> updateMainItemQuantity(String sessionId, String id, ActionType action) {
        return cartService.updateCartItemQuantity(sessionId, id, action)
                .thenReturn("redirect:/main/items");
    }

    public Mono<Void> updateCartItemQuantity(String sessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(sessionId, itemId, action);
    }

    public Mono<String> updateItemQuantity(String sessionId, String id, ActionType action) {
        return cartService.updateCartItemQuantity(sessionId, id, action)
                .thenReturn("redirect:/items/" + id);
    }

    public Mono<String> buy(String sessionId) {
        return orderService.createOrder(sessionId, cartService);
    }

    public Mono<List<OrderDto>> getOrders(String sessionId) {
        return orderService.getOrders(sessionId);
    }

    public Mono<OrderDto> getOrder(String id) {
        return orderService.getOrder(id);
    }

    public Flux<OrderItem> getOrderItems(String orderId) {
        return orderService.getOrderItems(orderId);
    }

    public Mono<Double> checkBalance() {
        return paymentService.checkBalance();
    }
}
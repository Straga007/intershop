package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CartService {
    Mono<List<ItemDto>> getCartItems(String sessionId);
    Mono<Void> updateCartItemQuantity(String sessionId, String itemId, ActionType action);
    Mono<Double> getCartTotal(String sessionId);
    Mono<Boolean> isCartEmpty(String sessionId);
    Mono<Void> clearCart(String sessionId);
}

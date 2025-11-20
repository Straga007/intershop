package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<String> createOrder(String sessionId, CartService cartService);
    Mono<List<OrderDto>> getOrders(String sessionId);
    Mono<OrderDto> getOrder(String id);
    Flux<OrderItem> getOrderItems(String orderId);
}
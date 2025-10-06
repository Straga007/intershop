package com.shop.spring.data.intershop.service;


import com.shop.spring.data.intershop.view.dto.OrderDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<String> createOrder(String sessionId);
    Mono<List<OrderDto>> getAllOrders();
    Mono<OrderDto> getOrderById(String id);
}

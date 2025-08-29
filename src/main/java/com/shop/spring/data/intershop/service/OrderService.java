package com.shop.spring.data.intershop.service;


import com.shop.spring.data.intershop.view.dto.OrderDto;

import java.util.List;

public interface OrderService {
    String createOrder(String sessionId);
    List<OrderDto> getAllOrders();
    OrderDto getOrderById(String id);
}

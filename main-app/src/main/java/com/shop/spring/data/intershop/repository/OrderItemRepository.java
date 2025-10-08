package com.shop.spring.data.intershop.repository;

import com.shop.spring.data.intershop.model.OrderItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
    @Query("SELECT id, item_id, quantity, order_id FROM order_items WHERE order_id = :orderId")
    Flux<OrderItem> findByOrderId(String orderId);
}
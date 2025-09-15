package com.shop.spring.data.intershop.repository;

import com.shop.spring.data.intershop.model.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    @Query("SELECT id, order_date FROM orders ORDER BY order_date DESC")
    Flux<Order> findAllByOrderByOrderDateDesc();
}
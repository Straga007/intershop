package com.shop.spring.data.intershop.repository;

import com.shop.spring.data.intershop.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, String> {
    Flux<Order> findAllByOrderByOrderDateDesc();
}
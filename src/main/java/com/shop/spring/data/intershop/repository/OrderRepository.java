package com.shop.spring.data.intershop.repository;

import com.shop.spring.data.intershop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByOrderByOrderDateDesc();
}

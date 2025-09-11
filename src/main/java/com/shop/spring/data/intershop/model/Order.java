package com.shop.spring.data.intershop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
public class Order {
    @Setter
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {
        this.id = UUID.randomUUID().toString();
        this.orderDate = LocalDateTime.now();
    }

    public Order(List<OrderItem> orderItems) {
        this.id = UUID.randomUUID().toString();
        this.orderDate = LocalDateTime.now();
        if (orderItems != null) {
            this.orderItems = orderItems;
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrder(this);
            }
        }
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        if (this.orderItems != null) {
            for (OrderItem orderItem : this.orderItems) {
                orderItem.setOrder(null);
            }
        }

        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();

        for (OrderItem orderItem : this.orderItems) {
            orderItem.setOrder(this);
        }
    }

    public double getTotalSum() {
        return orderItems.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }
}

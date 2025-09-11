package com.shop.spring.data.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Getter
@Table("orders")
public class Order {
    @Setter
    @Id
    private String id;

    private LocalDateTime orderDate;

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

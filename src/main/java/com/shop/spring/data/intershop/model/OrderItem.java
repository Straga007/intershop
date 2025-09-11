package com.shop.spring.data.intershop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Setter
@Getter
@Table("order_items")
public class OrderItem {
    @Id
    private String id;

    private Item item;

    private int quantity;

    private Order order;

    public OrderItem() {
    }

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }
}

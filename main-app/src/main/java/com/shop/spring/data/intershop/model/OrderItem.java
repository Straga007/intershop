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
    private Long id;

    @Column("item_id")
    private Long itemId;

    @Column("order_id")
    private String orderId;

    private int quantity;

    public OrderItem() {
    }
}
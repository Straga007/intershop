package com.shop.spring.data.intershop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
//Доменная модель, многие-ко-многим с атрибутами
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "quantity")
    private int quantity;

    public OrderItem() {}

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
    public double getTotalPrice() {
        return item != null ? item.getPrice() * quantity : 0.0;
    }
}

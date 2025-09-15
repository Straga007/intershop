package com.shop.spring.data.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table("orders")
public class Order implements Persistable<String> {
    @Id
    private String id;

    private LocalDateTime orderDate;

    public Order() {
        this.id = UUID.randomUUID().toString();
        this.orderDate = LocalDateTime.now();
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public double getTotalSum() {
        return 0.0;
    }
}
package com.shop.spring.data.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Table("orders")
public class Order implements Persistable<Long> {
    @Id
    private Long id;

    private LocalDateTime orderDate;

    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    public double getTotalSum() {
        return 0.0;
    }
}
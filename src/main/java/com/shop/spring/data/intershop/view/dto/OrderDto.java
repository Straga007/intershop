package com.shop.spring.data.intershop.view.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class OrderDto {
    private String id;
    private List<ItemDto> items;

    public OrderDto() {
    }

    public OrderDto(String id, List<ItemDto> items) {
        this.id = id;
        this.items = items;
    }

    public double totalSum() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getCount())
                .sum();
    }
}

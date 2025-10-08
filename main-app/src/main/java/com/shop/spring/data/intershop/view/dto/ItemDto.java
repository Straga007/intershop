package com.shop.spring.data.intershop.view.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemDto {
    private String id;
    private String title;
    private String description;
    private String image;
    private double price;
    private int count;

    public ItemDto() {
    }

    public ItemDto(String id, String title, String description, String image, double price, int count) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.price = price;
        this.count = count;
    }

}

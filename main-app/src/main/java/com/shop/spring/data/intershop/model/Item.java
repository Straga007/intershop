package com.shop.spring.data.intershop.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Setter
@Getter
@Table("items")
public class Item {
    @Id
    private Long id;

    private String title;
    private String description;
    private String image;
    private double price;
    private int count;

    public Item() {}

    public Item(String title, String description, String image, double price, int count) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.price = price;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

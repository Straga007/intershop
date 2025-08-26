package com.shop.spring.data.intershop.model;


import java.util.List;

public class Order {
    private String id;
    private List<Item> items;

    public Order(String id, List<Item> items) {
        this.id = id;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public double totalSum() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getCount())
                .sum();
    }
}

package com.shop.spring.data.intershop.model;

public class Item {
    private String id;
    private String title;
    private String description;
    private String image;
    private double price;
    private int count;

    public Item(String id, String title, String description, String imgPath, int count, double price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = imgPath;
        this.count = count;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

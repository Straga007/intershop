package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.Paging;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ShopRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class ShopService {
    private final ShopRepository shopRepository;

    private Map<String, Integer> cart = new HashMap<>();

    private Map<String, Order> orders = new HashMap<>();

    private int orderIdCounter = 1;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public List<List<Item>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        return new ArrayList<>();
    }


    public void updateMainItemQuantity(String itemId, ActionType action) {
        updateCartItemQuantity(itemId, action);
    }

    public List<Item> getCartItems() {
        return new ArrayList<>();
    }


    public void updateCartItemQuantity(String itemId, ActionType action) {
        int currentQuantity = cart.getOrDefault(itemId, 0);

        switch (action) {
            case PLUS:
                cart.put(itemId, currentQuantity + 1);
                break;
            case MINUS:
                if (currentQuantity > 1) {
                    cart.put(itemId, currentQuantity - 1);
                } else {
                    cart.remove(itemId);
                }
                break;
            case DELETE:
                cart.remove(itemId);
                break;
        }
    }


    public Item getItem(String id) {
        return null;
    }

    public void updateItemQuantity(String itemId, ActionType action) {
        updateCartItemQuantity(itemId, action);
    }

    public String buy() {
        if (cart.isEmpty()) {
            return null;
        }

        List<Item> orderItems = new ArrayList<>();

        String orderId = String.valueOf(orderIdCounter++);
        Order order = new Order(orderId, orderItems);
        orders.put(orderId, order);

        cart.clear();

        return orderId;
    }


    public List<Order> getOrders() {
        return new ArrayList<>(orders.values());
    }


    public Order getOrder(String id) {
        return orders.get(id);
    }


    public double getCartTotal() {
        return 0.0;
    }


    public boolean isCartEmpty() {
        return cart.isEmpty();
    }


    public Paging createPaging(int pageNumber, int pageSize, boolean hasNext) {
        boolean hasPrevious = pageNumber > 1;
        return new Paging(pageNumber, pageSize, hasNext, hasPrevious);
    }
}

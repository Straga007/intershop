package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
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

    private final Map<String, Integer> cart = new HashMap<>();

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public List<List<Item>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        List<Item> items;

        if (search != null && !search.isEmpty()) {
            items = shopRepository.findItemsBySearch(search);
        } else {
            items = shopRepository.findAllItems();
        }

        switch (sort) {
            case ALPHA:
                items.sort((i1, i2) -> i1.getTitle().compareTo(i2.getTitle()));
                break;
            case PRICE:
                items.sort((i1, i2) -> Double.compare(i1.getPrice(), i2.getPrice()));
                break;
            default:
                break;
        }

        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, items.size());

        if (startIndex >= items.size()) {
            return new ArrayList<>();
        }

        List<Item> pageItems = items.subList(startIndex, endIndex);
        List<List<Item>> result = new ArrayList<>();
        result.add(pageItems);

        return result;
    }

    public void updateMainItemQuantity(String itemId, ActionType action) {
        updateCartItemQuantity(itemId, action);
    }

    public List<Item> getCartItems() {
        List<Item> items = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            Item item = shopRepository.findItemById(itemId);
            if (item != null) {
                Item cartItem = new Item(
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getImage(),
                        quantity,
                        item.getPrice()
                );
                items.add(cartItem);
            }
        }

        return items;
    }

    public void updateCartItemQuantity(String itemId, ActionType action) {
        int currentQuantity = cart.getOrDefault(itemId, 0);
        Item item = shopRepository.findItemById(itemId);

        if (item == null) {
            return;
        }

        switch (action) {
            case PLUS:
                if (currentQuantity < item.getCount()) {
                    cart.put(itemId, currentQuantity + 1);
                }
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
        return shopRepository.findItemById(id);
    }

    public void updateItemQuantity(String itemId, ActionType action) {
        updateCartItemQuantity(itemId, action);
    }

    public String buy() {
        if (cart.isEmpty()) {
            return null;
        }

        List<Item> orderItems = getCartItems();

        String orderId = shopRepository.createOrder(orderItems);

        for (Item item : orderItems) {
            Item originalItem = shopRepository.findItemById(item.getId());
            if (originalItem != null) {
                int newCount = originalItem.getCount() - item.getCount();
                shopRepository.updateItemCount(item.getId(), newCount);
            }
        }

        cart.clear();

        return orderId;
    }

    public List<Order> getOrders() {
        return shopRepository.findAllOrders();
    }

    public Order getOrder(String id) {
        return shopRepository.findOrderById(id);
    }

    public double getCartTotal() {
        double total = 0.0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            Item item = shopRepository.findItemById(itemId);
            if (item != null) {
                total += item.getPrice() * quantity;
            }
        }

        return total;
    }

    public boolean isCartEmpty() {
        return cart.isEmpty();
    }
}

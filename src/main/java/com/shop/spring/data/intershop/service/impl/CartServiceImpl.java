package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final ShopMapper shopMapper;

    private final Map<String, Map<String, Integer>> sessionCarts = new ConcurrentHashMap<>();

    public CartServiceImpl(ItemRepository itemRepository, ShopMapper shopMapper) {
        this.itemRepository = itemRepository;
        this.shopMapper = shopMapper;
    }

    //корзина по sessionId
    private Map<String, Integer> getCart(String sessionId) {
        return sessionCarts.computeIfAbsent(sessionId, k -> new HashMap<>());
    }

    @Override
    public List<ItemDto> getCartItems(String sessionId) {
        List<ItemDto> items = new ArrayList<>();
        Map<String, Integer> cart = getCart(sessionId);

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            Item item = itemRepository.findById(Long.parseLong(itemId)).orElse(null);
            if (item != null) {
                Item cartItem = new Item();
                cartItem.setId(item.getId());
                cartItem.setTitle(item.getTitle());
                cartItem.setDescription(item.getDescription());
                cartItem.setImage(item.getImage());
                cartItem.setPrice(item.getPrice());
                cartItem.setCount(quantity);
                items.add(shopMapper.toItemDto(cartItem));
            }
        }

        return items;
    }

    @Override
    public void updateCartItemQuantity(String sessionId, String itemId, ActionType action) {
        Map<String, Integer> cart = getCart(sessionId);
        int currentQuantity = cart.getOrDefault(itemId, 0);
        Item item = itemRepository.findById(Long.parseLong(itemId)).orElse(null);

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

    @Override
    public double getCartTotal(String sessionId) {
        double total = 0.0;
        Map<String, Integer> cart = getCart(sessionId);

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            Item item = itemRepository.findById(Long.parseLong(itemId)).orElse(null);
            if (item != null) {
                total += item.getPrice() * quantity;
            }
        }

        return total;
    }

    @Override
    public boolean isCartEmpty(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        return cart.isEmpty();
    }

    @Override
    public void clearCart(String sessionId) {
        sessionCarts.remove(sessionId);
    }
}

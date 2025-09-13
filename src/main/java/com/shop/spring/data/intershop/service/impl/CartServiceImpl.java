package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    public Mono<List<ItemDto>> getCartItems(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        
        List<Mono<ItemDto>> itemMonos = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            
            Mono<ItemDto> itemDtoMono = itemRepository.findById(Long.parseLong(itemId))
                    .map(item -> {
                        Item cartItem = new Item();
                        cartItem.setId(item.getId());
                        cartItem.setTitle(item.getTitle());
                        cartItem.setDescription(item.getDescription());
                        cartItem.setImage(item.getImage());
                        cartItem.setPrice(item.getPrice());
                        cartItem.setCount(quantity);
                        return shopMapper.toItemDto(cartItem);
                    });
            
            itemMonos.add(itemDtoMono);
        }
        
        return Mono.zip(itemMonos, objects -> {
                    List<ItemDto> result = new ArrayList<>();
                    for (Object object : objects) {
                        if (object instanceof ItemDto) {
                            result.add((ItemDto) object);
                        }
                    }
                    return result;
                })
                .defaultIfEmpty(new ArrayList<>());
    }

    @Override
    public Mono<Void> updateCartItemQuantity(String sessionId, String itemId, ActionType action) {
        return itemRepository.findById(Long.parseLong(itemId))
            .doOnNext(item -> {
                Map<String, Integer> cart = getCart(sessionId);
                int currentQuantity = cart.getOrDefault(itemId, 0);

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
            })
            .then();
    }

    @Override
    public Mono<Double> getCartTotal(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        
        List<Mono<Double>> itemTotals = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            
            Mono<Double> itemTotal = itemRepository.findById(Long.parseLong(itemId))
                    .map(item -> item.getPrice() * quantity)
                    .defaultIfEmpty(0.0);
            
            itemTotals.add(itemTotal);
        }
        
        return Mono.zip(itemTotals, objects -> {
                    double total = 0.0;
                    for (Object object : objects) {
                        if (object instanceof Double) {
                            total += (Double) object;
                        }
                    }
                    return total;
                })
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Void> clearCart(String sessionId) {
        return Mono.fromRunnable(() -> getCart(sessionId).clear());
    }

    @Override
    public Mono<Boolean> isCartEmpty(String sessionId) {
        return Mono.just(getCart(sessionId).isEmpty());
    }

}

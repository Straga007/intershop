package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final ShopMapper shopMapper;

    // Временно используем одну общую корзину для всего приложения
    private final Map<String, Integer> sharedCart = new ConcurrentHashMap<>();

    public CartServiceImpl(ItemRepository itemRepository, ShopMapper shopMapper) {
        this.itemRepository = itemRepository;
        this.shopMapper = shopMapper;
    }

    //корзина по sessionId
    private Map<String, Integer> getCart(String ignoredSessionId) {
        return sharedCart;
    }

    @Override
    public Mono<List<ItemDto>> getCartItems(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        
        System.out.println("Получение содержимого корзины для сессии: " + sessionId);
        System.out.println("Содержимое корзины: " + cart);
        
        if (cart.isEmpty()) {
            System.out.println("Корзина пуста");
            return Mono.just(new ArrayList<>());
        }
        
        // Получаем все товары пакетно
        List<Long> itemIds = cart.keySet().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        
        return itemRepository.findAllById(itemIds)
                .filter(item -> cart.containsKey(item.getId().toString()))
                .map(item -> {
                    Item cartItem = new Item();
                    cartItem.setId(item.getId());
                    cartItem.setTitle(item.getTitle());
                    cartItem.setDescription(item.getDescription());
                    cartItem.setImage(item.getImage());
                    cartItem.setPrice(item.getPrice());
                    cartItem.setCount(cart.get(item.getId().toString()));
                    return cartItem;
                })
                .map(shopMapper::toItemDto)
                .collectList()
                .onErrorReturn(new ArrayList<>());
    }

    @Override
    public Mono<Void> updateCartItemQuantity(String sessionId, String itemId, ActionType actionType) {
        Map<String, Integer> cart = getCart(sessionId);
        
        System.out.println("Перед обновлением корзины: " + cart);
        System.out.println("Session ID: " + sessionId);
        System.out.println("Item ID: " + itemId);
        System.out.println("Action type: " + actionType);
        
        return itemRepository.findById(Long.valueOf(itemId))
                .doOnNext(item -> {
                    int currentQuantity = cart.getOrDefault(itemId, 0);
                    int newQuantity;
                    
                    switch (actionType) {
                        case PLUS:
                            newQuantity = currentQuantity + 1;
                            cart.put(itemId, newQuantity);
                            break;
                        case MINUS:
                            newQuantity = Math.max(0, currentQuantity - 1);
                            if (newQuantity == 0) {
                                cart.remove(itemId);
                            } else {
                                cart.put(itemId, newQuantity);
                            }
                            break;
                        case DELETE:
                            cart.remove(itemId);
                            break;
                        default:
                            // Ничего не делаем для неизвестных действий
                            break;
                    }
                    
                    System.out.println("После обновления корзины: " + cart);
                })
                .then();
    }

    @Override
    public Mono<Double> getCartTotal(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        
        if (cart.isEmpty()) {
            return Mono.just(0.0);
        }
        
        // Получаем все товары пакетно
        List<Long> itemIds = cart.keySet().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        
        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId, item -> item.getPrice() * cart.get(item.getId().toString()))
                .map(priceMap -> priceMap.values().stream().mapToDouble(Double::doubleValue).sum())
                .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Boolean> isCartEmpty(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        return Mono.just(cart.isEmpty());
    }

    @Override
    public Mono<Void> clearCart(String sessionId) {
        Map<String, Integer> cart = getCart(sessionId);
        cart.clear();
        return Mono.empty();
    }
}
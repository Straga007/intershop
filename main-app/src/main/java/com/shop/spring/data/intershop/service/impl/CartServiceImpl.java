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
        
        return Flux.fromStream(cart.entrySet().stream())
                .flatMap(entry -> {
                    String itemId = entry.getKey();
                    int quantity = entry.getValue();
                    
                    System.out.println("Обработка товара: ID=" + itemId + ", количество=" + quantity);
                    
                    return itemRepository.findById(Long.parseLong(itemId))
                            .map(item -> {
                                Item cartItem = new Item();
                                cartItem.setId(item.getId());
                                cartItem.setTitle(item.getTitle());
                                cartItem.setDescription(item.getDescription());
                                cartItem.setImage(item.getImage());
                                cartItem.setPrice(item.getPrice());
                                cartItem.setCount(quantity);
                                return cartItem;
                            })
                            .map(shopMapper::toItemDto)
                            .switchIfEmpty(Mono.just(new ItemDto())); // Обработка отсутствующих товаров
                })
                .filter(itemDto -> itemDto.getId() != null) // Исключаем несуществующие товары
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
        
        List<Mono<Double>> prices = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            
            Mono<Double> priceMono = itemRepository.findById(Long.valueOf(itemId))
                    .map(item -> item.getPrice() * quantity);
            prices.add(priceMono);
        }
        
        return Mono.zip(prices, objects -> {
                    double total = 0.0;
                    for (Object obj : objects) {
                        total += (Double) obj;
                    }
                    return total;
                })
                .onErrorReturn(0.0)
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
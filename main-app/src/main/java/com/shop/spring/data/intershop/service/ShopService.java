package com.shop.spring.data.intershop.service;

import com.shop.main.client.api.DefaultApi;
import com.shop.main.client.model.BalanceResponse;
import com.shop.spring.data.intershop.model.CartItem;
import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ShopMapper shopMapper;
    private final DefaultApi paymentsApi;
    private final R2dbcEntityTemplate template;

    private final Map<String, List<CartItem>> userCarts = new ConcurrentHashMap<>();

    public Mono<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        Pageable pageable = switch (sort) {
            case PRICE -> PageRequest.of(pageNumber - 1, pageSize, Sort.by("price").descending());
            case ALPHA -> PageRequest.of(pageNumber - 1, pageSize, Sort.by("title").ascending());
            default -> PageRequest.of(pageNumber - 1, pageSize);
        };

        Flux<Item> items;
        if (search.isEmpty()) {
            items = itemRepository.findAllItems();
        } else {
            items = itemRepository.findByTitleOrDescriptionContaining(search);
        }

        return items.map(shopMapper::toItemDto)
                .skip((long) (pageNumber - 1) * pageSize)
                .take(pageSize)
                .collectList();
    }

    public Mono<ItemDto> getItem(String id) {
        return itemRepository.findById(Long.valueOf(id))
                .map(shopMapper::toItemDto);
    }

    public Mono<List<ItemDto>> getCartItems(String sessionId) {
        return Mono.fromCallable(() -> {
            List<CartItem> cartItems = userCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());
            return cartItems.stream()
                    .map(cartItem -> {
                        ItemDto itemDto = shopMapper.toItemDto(cartItem.getItem());
                        itemDto.setQuantity(cartItem.getQuantity());
                        return itemDto;
                    })
                    .collect(Collectors.toList());
        });
    }

    public Mono<Double> getCartTotal(String sessionId) {
        return Mono.fromCallable(() -> {
            List<CartItem> cartItems = userCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());
            return cartItems.stream()
                    .mapToDouble(cartItem -> cartItem.getItem().getPrice() * cartItem.getQuantity())
                    .sum();
        });
    }

    public Mono<Boolean> isCartEmpty(String sessionId) {
        return Mono.fromCallable(() -> {
            List<CartItem> cartItems = userCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());
            return cartItems.isEmpty();
        });
    }

    public Mono<String> updateMainItemQuantity(String sessionId, String id, ActionType action) {
        return itemRepository.findById(Long.valueOf(id))
                .map(item -> {
                    List<CartItem> cartItems = userCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());
                    Optional<CartItem> existingItem = cartItems.stream()
                            .filter(cartItem -> cartItem.getItem().getId().equals(item.getId()))
                            .findFirst();

                    if (existingItem.isPresent()) {
                        CartItem cartItem = existingItem.get();
                        switch (action) {
                            case PLUS:
                                cartItem.setQuantity(cartItem.getQuantity() + 1);
                                break;
                            case MINUS:
                                if (cartItem.getQuantity() > 1) {
                                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                                } else {
                                    cartItems.remove(cartItem);
                                }
                                break;
                            case DELETE:
                                cartItems.remove(cartItem);
                                break;
                        }
                    } else if (action == ActionType.PLUS) {
                        cartItems.add(new CartItem(item, 1));
                    }

                    return "redirect:/main/items";
                });
    }

    public Mono<String> updateCartItemQuantity(String sessionId, String id, ActionType action) {
        // В реальной реализации sessionId будет заменен на userId
        return Mono.empty();
    }

    public Mono<String> updateItemQuantity(String sessionId, String id, ActionType action) {
        // В реальной реализации sessionId будет заменен на userId
        return Mono.empty();
    }

    public Mono<String> buy(String sessionId) {
        // В реальной реализации sessionId будет заменен на userId
        return Mono.empty();
    }

    public Mono<List<Order>> getOrders(String sessionId) {
        // В реальной реализации sessionId будет заменен на userId
        return Mono.just(List.of());
    }

    public Mono<Order> getOrder(String id) {
        return orderRepository.findById(id);
    }

    public Mono<Double> checkBalance() {
        log.info("checkBalance");
        return paymentsApi.getBalance()
                .map(BalanceResponse::getBalance)
                .doOnNext(balance -> log.info("Получен баланс: {}", balance))
                .doOnError(error -> log.error("Ошибка при получении баланса: ", error))
                .onErrorResume(error -> {
                    log.error("Не удалось получить баланс, возвращаем значение по умолчанию 0.0: ", error);
                    return Mono.just(0.0);
                });
    }
}
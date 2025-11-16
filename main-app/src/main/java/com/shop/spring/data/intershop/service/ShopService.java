package com.shop.spring.data.intershop.service;

import com.shop.main.client.api.DefaultApi;
import com.shop.main.client.model.BalanceResponse;
import com.shop.spring.data.intershop.model.CartItem;
import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import java.util.Arrays;
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
    private final CartService cartService;
    private final DefaultApi paymentsApi;
    private final OrderItemRepository orderItemRepository;
    private final R2dbcEntityTemplate template;

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
        return cartService.getCartItems(sessionId);
    }

    public Mono<Double> getCartTotal(String sessionId) {
        return cartService.getCartTotal(sessionId);
    }

    public Mono<Boolean> isCartEmpty(String sessionId) {
        return cartService.isCartEmpty(sessionId);
    }

    public Mono<String> updateMainItemQuantity(String sessionId, String id, ActionType action) {
        return itemRepository.findById(Long.valueOf(id))
                .flatMap(item -> {
                    return cartService.updateCartItemQuantity(sessionId, id, action)
                            .thenReturn("redirect:/main/items");
                });
    }

    public Mono<Void> updateCartItemQuantity(String sessionId, String itemId, ActionType action) {
        return cartService.updateCartItemQuantity(sessionId, itemId, action);
    }

    public Mono<String> updateItemQuantity(String sessionId, String id, ActionType action) {
        return cartService.updateCartItemQuantity(sessionId, id, action)
                .thenReturn("redirect:/items/" + id);
    }

    public Mono<String> buy(String sessionId) {
        log.info("Начинаем оформление заказа для сессии: {}", sessionId);
        return cartService.getCartItems(sessionId)
                .doOnNext(items -> log.info("Получены товары из корзины: {}", items))
                .flatMap(items -> {
                    if (items.isEmpty()) {
                        log.info("Корзина пуста, заказ не будет создан");
                        return Mono.empty();
                    }

                    // Создаем заказ
                    Order order = new Order();
                    order.setOrderDate(LocalDateTime.now());
                    order.setUserId(sessionId); // Сохраняем ID пользователя
                    log.info("Создан заказ: {}", order);

                    // Сохраняем заказ
                    return orderRepository.save(order)
                            .doOnNext(savedOrder -> log.info("Заказ сохранен в БД: {}", savedOrder))
                            .flatMap(savedOrder -> {
                                // Создаем элементы заказа
                                List<OrderItem> orderItems = items.stream()
                                        .map(itemDto -> {
                                            OrderItem orderItem = new OrderItem();
                                            orderItem.setItemId(Long.valueOf(itemDto.getId()));
                                            orderItem.setOrderId(savedOrder.getId());
                                            orderItem.setQuantity(itemDto.getCount());
                                            log.info("Создан элемент заказа: {}", orderItem);
                                            return orderItem;
                                        })
                                        .collect(Collectors.toList());

                                // Сохраняем элементы заказа
                                return Flux.fromIterable(orderItems)
                                        .flatMap(orderItemRepository::save)
                                        .doOnNext(savedOrderItem -> log.info("Элемент заказа сохранен: {}", savedOrderItem))
                                        .then(cartService.clearCart(sessionId))
                                        .doOnSuccess(v -> log.info("Корзина очищена"))
                                        .thenReturn(savedOrder.getId());
                            });
                })
                .doOnError(error -> log.error("Ошибка при оформлении заказа: ", error));
    }

    public Mono<List<Order>> getOrders(String sessionId) {
        return orderRepository.findAll()
                .filter(order -> sessionId.equals(order.getUserId()))
                .collectList();
    }

    public Mono<OrderDto> getOrder(String id) {
        return orderRepository.findById(id)
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .flatMap(orderItems -> {
                                    if (orderItems.isEmpty()) {
                                        OrderDto orderDto = new OrderDto();
                                        orderDto.setId(order.getId());
                                        orderDto.setItems(new ArrayList<>());
                                        return Mono.just(orderDto);
                                    }

                                    // Загружаем товары для каждого элемента заказа
                                    List<Mono<Item>> itemMonos = orderItems.stream()
                                            .map(orderItem -> itemRepository.findById(orderItem.getItemId()))
                                            .collect(Collectors.toList());

                                    return Mono.zip(itemMonos, itemsArray -> {
                                        List<Item> items = new ArrayList<>();
                                        for (Object item : itemsArray) {
                                            if (item instanceof Item) {
                                                items.add((Item) item);
                                            }
                                        }
                                        return items;
                                    }).map(items -> {
                                        // Создаем OrderDto с элементами
                                        List<ItemDto> itemDtos = new ArrayList<>();

                                        for (int i = 0; i < items.size(); i++) {
                                            Item item = items.get(i);
                                            OrderItem orderItem = orderItems.get(i);

                                            ItemDto itemDto = shopMapper.toItemDto(item);
                                            itemDto.setCount(orderItem.getQuantity());
                                            itemDtos.add(itemDto);
                                        }

                                        OrderDto orderDto = new OrderDto();
                                        orderDto.setId(order.getId());
                                        orderDto.setItems(itemDtos);
                                        return orderDto;
                                    });
                                })
                );
    }

    public Flux<OrderItem> getOrderItems(String orderId) {
        return orderItemRepository.findByOrderId(orderId);
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
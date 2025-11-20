package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.service.OrderService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopMapper shopMapper;
    private final R2dbcEntityTemplate template;

    @Override
    public Mono<String> createOrder(String sessionId, CartService cartService) {
        log.info("Начинаем оформление заказа для сессии: {}", sessionId);
        return cartService.getCartItems(sessionId)
                .doOnNext(items -> log.info("Получены товары из корзины: {}", items))
                .flatMap(items -> {
                    if (items.isEmpty()) {
                        log.info("Корзина пуста, заказ не будет создан");
                        return Mono.empty();
                    }

                    Order order = new Order();
                    order.setOrderDate(LocalDateTime.now());
                    order.setUserId(sessionId); // Сохраняем ID
                    log.info("Создан заказ: {}", order);

                    return orderRepository.save(order)
                            .doOnNext(savedOrder -> log.info("Заказ сохранен в БД: {}", savedOrder))
                            .flatMap(savedOrder -> {
                                //элементы заказа
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

    @Override
    public Mono<List<OrderDto>> getOrders(String sessionId) {
        return orderRepository.findAll()
                .filter(order -> sessionId.equals(order.getUserId()))
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

                            // Загружаем товары для каждого OrderItem
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
                                // Создаем OrderDto
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
                )
                .collectList();
    }

    @Override
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

                            // Загружаем товары для каждого OrderItem
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
                                // Создаем OrderDto
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

    @Override
    public Flux<OrderItem> getOrderItems(String orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}
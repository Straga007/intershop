package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.repository.OrderItemRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.service.OrderService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ShopMapper shopMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ItemRepository itemRepository, CartService cartService, ShopMapper shopMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.cartService = cartService;
        this.shopMapper = shopMapper;
    }

    @Override
    public Mono<String> createOrder(String sessionId) {
        return cartService.isCartEmpty(sessionId)
                .filter(empty -> !empty.booleanValue())
                .flatMap(empty -> cartService.getCartItems(sessionId))
                .flatMap(cartItems -> {
                    Order order = new Order();
                    List<Mono<OrderItem>> orderItemMonos = new ArrayList<>();
                    
                    for (ItemDto itemDto : cartItems) {
                        Mono<OrderItem> orderItemMono = itemRepository.findById(Long.valueOf(itemDto.getId()))
                                .flatMap(item -> {
                                    int newCount = item.getCount() - itemDto.getCount();
                                    item.setCount(newCount);
                                    return itemRepository.save(item);
                                })
                                .map(item -> {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setItem(item);
                                    orderItem.setQuantity(itemDto.getCount());
                                    order.addOrderItem(orderItem);
                                    return orderItem;
                                });
                        
                        orderItemMonos.add(orderItemMono);
                    }
                    
                    return Mono.when(orderItemMonos)
                            .then(orderRepository.save(order))
                            .flatMap(savedOrder -> cartService.clearCart(sessionId)
                                    .thenReturn(savedOrder.getId()));
                })
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<List<OrderDto>> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .flatMap(order -> 
                    orderItemRepository.findByOrderId(order.getId())
                        .flatMap(orderItem -> 
                            itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return orderItem;
                                })
                        )
                        .collectList()
                        .map(orderItems -> {
                            order.setOrderItems(orderItems);
                            return order;
                        })
                )
                .map(shopMapper::toOrderDto)
                .collectList();
    }

    @Override
    public Mono<OrderDto> getOrderById(String id) {
        return orderRepository.findById(id)
                .flatMap(order ->
                    orderItemRepository.findByOrderId(order.getId())
                        .flatMap(orderItem ->
                            itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return orderItem;
                                })
                        )
                        .collectList()
                        .map(orderItems -> {
                            order.setOrderItems(orderItems);
                            return order;
                        })
                )
                .map(shopMapper::toOrderDto)
                .switchIfEmpty(Mono.empty());
    }
}

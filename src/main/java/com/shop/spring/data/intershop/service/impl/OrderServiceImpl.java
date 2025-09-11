package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.service.OrderService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ShopMapper shopMapper;

    public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository, CartService cartService, ShopMapper shopMapper) {
        this.orderRepository = orderRepository;
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

                    // Обработка элементов заказа
                    for (ItemDto itemDto : cartItems) {
                        Item item = itemRepository.findById(Long.valueOf(itemDto.getId())).block();
                        if (item != null) {
                            int newCount = item.getCount() - itemDto.getCount();
                            item.setCount(newCount);
                            itemRepository.save(item).block();

                            OrderItem orderItem = new OrderItem();
                            orderItem.setItem(item);
                            orderItem.setQuantity(itemDto.getCount());
                            order.addOrderItem(orderItem);
                        }
                    }

                    return orderRepository.save(order)
                            .flatMap(savedOrder -> cartService.clearCart(sessionId)
                                    .thenReturn(savedOrder.getId()));
                })
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<List<OrderDto>> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .map(shopMapper::toOrderDto)
                .collectList();
    }

    @Override
    public Mono<OrderDto> getOrderById(String id) {
        return orderRepository.findById(id)
                .map(shopMapper::toOrderDto)
                .switchIfEmpty(Mono.empty());
    }
}

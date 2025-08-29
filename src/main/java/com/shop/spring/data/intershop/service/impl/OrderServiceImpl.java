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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public String createOrder(String sessionId) {
        if (cartService.isCartEmpty(sessionId)) {
            return null;
        }

        List<ItemDto> cartItems = cartService.getCartItems(sessionId);

        Order order = new Order();

        for (ItemDto itemDto : cartItems) {
            Item item = itemRepository.findById(Long.valueOf(itemDto.getId())).orElse(null);
            if (item != null) {
                int newCount = item.getCount() - itemDto.getCount();
                item.setCount(newCount);
                itemRepository.save(item);

                OrderItem orderItem = new OrderItem();
                orderItem.setItem(item);
                orderItem.setQuantity(itemDto.getCount());
                order.addOrderItem(orderItem);
            }
        }

        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(sessionId);

        return savedOrder.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return shopMapper.toOrderDtos(orderRepository.findAllByOrderByOrderDateDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(String id) {
        return shopMapper.toOrderDto(orderRepository.findById(id).orElse(null));
    }
}

package com.shop.spring.data.intershop.view.mapper;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShopMapper {

    public ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemDto(
                item.getId() != null ? item.getId().toString() : null,
                item.getTitle(),
                item.getDescription(),
                item.getImage(),
                item.getPrice(),
                item.getCount()
        );
    }

    public Item toItem(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }
        Item item = new Item();
        item.setId(itemDto.getId() != null ? Long.parseLong(itemDto.getId()) : null);
        item.setTitle(itemDto.getTitle());
        item.setDescription(itemDto.getDescription());
        item.setImage(itemDto.getImage());
        item.setPrice(itemDto.getPrice());
        item.setCount(itemDto.getCount());
        return item;
    }

    public List<ItemDto> toItemDtos(List<Item> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    public List<Item> toItems(List<ItemDto> itemDtos) {
        if (itemDtos == null) {
            return null;
        }
        return itemDtos.stream()
                .map(this::toItem)
                .collect(Collectors.toList());
    }

    public OrderDto toOrderDto(Order order) {
        if (order == null) {
            return null;
        }

        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setItems(new ArrayList<>());
        return orderDto;
    }

    public Order toOrder(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }

        Order order = new Order();
        order.setId(orderDto.getId());
        // orderItems устанавливаются отдельно
        return order;
    }

    public List<OrderDto> toOrderDtos(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(this::toOrderDto)
                .collect(Collectors.toList());
    }

    public List<Order> toOrders(List<OrderDto> orderDtos) {
        if (orderDtos == null) {
            return null;
        }
        return orderDtos.stream()
                .map(this::toOrder)
                .collect(Collectors.toList());
    }
}
package com.shop.spring.data.intershop.view.mapper;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import org.springframework.stereotype.Component;

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
    public List<ItemDto> toItemDtos(List<Item> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

}
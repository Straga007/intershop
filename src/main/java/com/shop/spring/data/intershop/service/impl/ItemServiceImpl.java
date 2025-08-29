package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.ItemService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ShopMapper shopMapper;

    public ItemServiceImpl(ItemRepository itemRepository, ShopMapper shopMapper) {
        this.itemRepository = itemRepository;
        this.shopMapper = shopMapper;
    }

    @Override
    public List<ItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return shopMapper.toItemDtos(items);
    }

    @Override
    public ItemDto getItemById(String id) {
        Item item = itemRepository.findById(Long.parseLong(id)).orElse(null);
        return shopMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItems(String searchQuery) {
        List<Item> items = itemRepository.findByTitleOrDescriptionContaining(searchQuery);
        return shopMapper.toItemDtos(items);
    }

    @Override
    public List<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        List<Item> items;

        if (search != null && !search.isEmpty()) {
            items = itemRepository.findByTitleOrDescriptionContaining(search);
        } else {
            items = itemRepository.findAll();
        }

        switch (sort) {
            case ALPHA:
                items.sort((i1, i2) -> i1.getTitle().compareTo(i2.getTitle()));
                break;
            case PRICE:
                items.sort((i1, i2) -> Double.compare(i1.getPrice(), i2.getPrice()));
                break;
            default:
                break;
        }

        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, items.size());

        if (startIndex >= items.size()) {
            return new ArrayList<>();
        }

        List<Item> pageItems = items.subList(startIndex, endIndex);
        List<ItemDto> pageItemDtos = shopMapper.toItemDtos(pageItems);

        List<List<ItemDto>> result = new ArrayList<>();
        result.add(pageItemDtos);

        return result;
    }

    @Override
    public void updateItemQuantity(String sessionId, String itemId, ActionType action) {
        throw new UnsupportedOperationException("Use CartService for cart operations");
    }
}

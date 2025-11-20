package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.CatalogService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {
    private final ItemRepository itemRepository;
    private final ShopMapper shopMapper;

    @Override
    public Mono<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        Flux<Item> items;
        if (search.isEmpty()) {
            items = itemRepository.findAllItems();
        } else {
            items = itemRepository.findByTitleOrDescriptionContaining(search);
        }

        switch (sort) {
            case ALPHA:
                items = items.sort(Comparator.comparing(Item::getTitle));
                break;
            case NAME_ASC:
                items = items.sort(Comparator.comparing(Item::getTitle));
                break;
            case NAME_DESC:
                items = items.sort(Comparator.comparing(Item::getTitle).reversed());
                break;
            case PRICE:
                items = items.sort(Comparator.comparing(Item::getPrice));
                break;
            case PRICE_ASC:
                items = items.sort(Comparator.comparing(Item::getPrice));
                break;
            case PRICE_DESC:
                items = items.sort(Comparator.comparing(Item::getPrice).reversed());
                break;
            default:
                break;
        }

        return items.skip((long) (pageNumber - 1) * pageSize)
                .take(pageSize)
                .map(shopMapper::toItemDto)
                .collectList();
    }

    @Override
    public Mono<ItemDto> getItem(String id) {
        return itemRepository.findById(Long.valueOf(id))
                .map(shopMapper::toItemDto);
    }

    @Override
    public Flux<Item> findAllItems() {
        return itemRepository.findAllItems();
    }

    @Override
    public Flux<Item> findByTitleOrDescriptionContaining(String search) {
        return itemRepository.findByTitleOrDescriptionContaining(search);
    }
}
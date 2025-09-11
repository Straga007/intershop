package com.shop.spring.data.intershop.service.impl;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.ItemService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ShopMapper shopMapper;

    public ItemServiceImpl(ItemRepository itemRepository, ShopMapper shopMapper) {
        this.itemRepository = itemRepository;
        this.shopMapper = shopMapper;
    }

    @Override
    public Mono<List<ItemDto>> getAllItems() {
        return itemRepository.findAllItems()
                .map(shopMapper::toItemDto)
                .collectList();
    }

    @Override
    public Mono<ItemDto> getItemById(String id) {
        return itemRepository.findById(Long.parseLong(id))
                .map(shopMapper::toItemDto)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<List<ItemDto>> searchItems(String searchQuery) {
        return itemRepository.findByTitleOrDescriptionContaining(searchQuery)
                .map(shopMapper::toItemDto)
                .collectList();
    }

    @Override
    public Mono<List<List<ItemDto>>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        Flux<Item> itemsFlux;

        if (search != null && !search.isEmpty()) {
            itemsFlux = itemRepository.findByTitleOrDescriptionContaining(search);
        } else {
            itemsFlux = itemRepository.findAllItems();
        }

        return itemsFlux.collectList()
                .map(items -> {
                    switch (sort) {
                        case ALPHA:
                            items.sort(Comparator.comparing(Item::getTitle));
                            break;
                        case PRICE:
                            items.sort(Comparator.comparing(Item::getPrice));
                            break;
                        default:
                            break;
                    }

                    int startIndex = (pageNumber - 1) * pageSize;
                    int endIndex = Math.min(startIndex + pageSize, items.size());

                    if (startIndex >= items.size()) {
                        return new ArrayList<List<ItemDto>>();
                    }

                    List<Item> pageItems = items.subList(startIndex, endIndex);
                    List<ItemDto> pageItemDtos = shopMapper.toItemDtos(pageItems);

                    List<List<ItemDto>> result = new ArrayList<>();
                    result.add(pageItemDtos);

                    return result;
                });
    }

    @Override
    public Mono<Void> updateItemQuantity(String sessionId, String itemId, ActionType action) {
        return Mono.error(new UnsupportedOperationException("Use CartService for cart operations"));
    }
}

package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.model.enums.SortType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CatalogService {
    Mono<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber);
    Mono<ItemDto> getItem(String id);
    Flux<Item> findAllItems();
    Flux<Item> findByTitleOrDescriptionContaining(String search);
}
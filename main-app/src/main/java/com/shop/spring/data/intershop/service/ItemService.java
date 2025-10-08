package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ItemService {
    Mono<ItemDto> getItemById(String id);
    Mono<List<List<ItemDto>>> getMainItems(String search, SortType sort, int pageSize, int pageNumber);
}

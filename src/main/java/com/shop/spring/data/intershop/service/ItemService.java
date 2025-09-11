package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ItemService {
    Mono<List<ItemDto>> getAllItems();
    Mono<ItemDto> getItemById(String id);
    Mono<List<ItemDto>> searchItems(String searchQuery);
    Mono<List<List<ItemDto>>> getMainItems(String search, SortType sort, int pageSize, int pageNumber);
    Mono<Void> updateItemQuantity(String sessionId, String itemId, ActionType action);
}

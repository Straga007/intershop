package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.view.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems();
    ItemDto getItemById(String id);
    List<ItemDto> searchItems(String searchQuery);
    List<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber);
    void updateItemQuantity(String sessionId, String itemId, ActionType action);
}

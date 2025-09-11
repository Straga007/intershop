package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopService {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    public ShopService(ItemService itemService, CartService cartService, OrderService orderService) {
        this.itemService = itemService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    public List<List<ItemDto>> getMainItems(String search, SortType sort, int pageSize, int pageNumber) {
        return itemService.getMainItems(search, sort, pageSize, pageNumber);
    }

    public void updateMainItemQuantity(String sessionId, String itemId, ActionType action) {
        cartService.updateCartItemQuantity(sessionId, itemId, action);
    }

    public List<ItemDto> getCartItems(String sessionId) {
        return cartService.getCartItems(sessionId);
    }

    public void updateCartItemQuantity(String sessionId, String itemId, ActionType action) {
        cartService.updateCartItemQuantity(sessionId, itemId, action);
    }

    public ItemDto getItem(String id) {
        return itemService.getItemById(id);
    }

    public void updateItemQuantity(String sessionId, String itemId, ActionType action) {
        cartService.updateCartItemQuantity(sessionId, itemId, action);
    }

    public String buy(String sessionId) {
        return orderService.createOrder(sessionId);
    }

    public List<OrderDto> getOrders() {
        return orderService.getAllOrders();
    }

    public OrderDto getOrder(String id) {
        return orderService.getOrderById(id);
    }

    public double getCartTotal(String sessionId) {
        return cartService.getCartTotal(sessionId);
    }

    public boolean isCartEmpty(String sessionId) {
        return cartService.isCartEmpty(sessionId);
    }
}

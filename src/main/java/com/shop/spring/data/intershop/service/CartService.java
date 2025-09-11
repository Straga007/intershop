package com.shop.spring.data.intershop.service;

import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.view.dto.ItemDto;

import java.util.List;

public interface CartService {
    List<ItemDto> getCartItems(String sessionId);
    void updateCartItemQuantity(String sessionId, String itemId, ActionType action);
    double getCartTotal(String sessionId);
    boolean isCartEmpty(String sessionId);
    void clearCart(String sessionId);
}

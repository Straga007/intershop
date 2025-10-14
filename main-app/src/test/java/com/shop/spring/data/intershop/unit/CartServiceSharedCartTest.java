//package com.shop.spring.data.intershop.unit;
//
//import com.shop.spring.data.intershop.model.Item;
//import com.shop.spring.data.intershop.model.enums.ActionType;
//import com.shop.spring.data.intershop.repository.ItemRepository;
//import com.shop.spring.data.intershop.service.impl.CartServiceImpl;
//import com.shop.spring.data.intershop.view.dto.ItemDto;
//import com.shop.spring.data.intershop.view.mapper.ShopMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CartServiceSharedCartTest {
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @Mock
//    private ShopMapper shopMapper;
//
//    private CartServiceImpl cartService;
//
//    @BeforeEach
//    void setUp() {
//        cartService = new CartServiceImpl(itemRepository, shopMapper);
//    }
//
//    @Test
//    void testCartIsSharedBetweenUsers() {
//        Item item = new Item();
//        item.setId(1L);
//        item.setTitle("Тестовый товар");
//        item.setCount(10);
//        item.setPrice(100.0);
//
//        ItemDto itemDto = new ItemDto();
//        itemDto.setId("1");
//        itemDto.setTitle("Тестовый товар");
//        itemDto.setCount(1);
//        itemDto.setPrice(100.0);
//
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
//        when(shopMapper.toItemDto(item)).thenReturn(itemDto);
//
//        String sessionId1 = "user1-session";
//        cartService.updateCartItemQuantity(sessionId1, "1", ActionType.PLUS);
//
//        List<ItemDto> user1Cart = cartService.getCartItems(sessionId1);
//        assertFalse(user1Cart.isEmpty(), "Корзина пользователя 1 должна содержать товары");
//        assertEquals(1, user1Cart.size(), "Корзина пользователя 1 должна содержать 1 товар");
//        assertEquals("1", user1Cart.getFirst().getId(), "ID товара должен быть 1");
//        assertEquals(1, user1Cart.getFirst().getCount(), "Количество товара должно быть 1");
//
//        double user1Total = cartService.getCartTotal(sessionId1);
//        assertEquals(100.0, user1Total, 0.01, "Общая стоимость корзины пользователя 1 должна быть 100.0");
//
//        String sessionId2 = "user2-session";
//        List<ItemDto> user2Cart = cartService.getCartItems(sessionId2);
//
//        assertEquals(0, user2Cart.size(), "Корзина пользователя 2 содержит 0 товаров ");
//        assertEquals(0, user2Cart.size(), "Количество товара в корзине пользователя 2 равно 0");
//
//        double user2Total = cartService.getCartTotal(sessionId2);
//        assertEquals(0, user2Total, 0.01, "Общая стоимость корзины пользователя 2 равна 0");
//
//        cartService.updateCartItemQuantity(sessionId2, "1", ActionType.PLUS);
//
//        user1Cart = cartService.getCartItems(sessionId1);
//        assertEquals(1, user1Cart.size(), "Корзина пользователя 1 должна содержать 1 товар");
//
//        user1Total = cartService.getCartTotal(sessionId1);
//        assertEquals(100.0, user1Total, 0.01, "Стоимость корзины пользователя 1 стала равна 100.0");
//    }
//
//    @Test
//    void testCartIsNotIsolatedBetweenSessions() {
//        Item item = new Item();
//        item.setId(1L);
//        item.setTitle("Тестовый товар");
//        item.setCount(10);
//        item.setPrice(100.0);
//
//        ItemDto itemDto1 = new ItemDto();
//        itemDto1.setId("1");
//        itemDto1.setTitle("Тестовый товар");
//        itemDto1.setCount(1);
//        itemDto1.setPrice(100.0);
//
//        ItemDto itemDto2 = new ItemDto();
//        itemDto2.setId("1");
//        itemDto2.setTitle("Тестовый товар");
//        itemDto2.setCount(2);
//        itemDto2.setPrice(100.0);
//
//        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
//        when(shopMapper.toItemDto(item)).thenReturn(itemDto1, itemDto2);
//
//        String sessionId1 = "session-1";
//        cartService.updateCartItemQuantity(sessionId1, "1", ActionType.PLUS);
//
//        String sessionId2 = "session-2";
//        cartService.updateCartItemQuantity(sessionId2, "1", ActionType.PLUS);
//
//        List<ItemDto> session1Cart = cartService.getCartItems(sessionId1);
//        assertFalse(session1Cart.isEmpty(), "Корзина в сессии 1 не должна быть пустой");
//        assertEquals(1, session1Cart.size(), "Корзина в сессии 1 должна содержать 1 товар");
//
//        assertEquals(1, session1Cart.getFirst().getCount(),
//                "Количество товара в сессии 1 равно 1 поскольку разные сессии");
//
//        List<ItemDto> session2Cart = cartService.getCartItems(sessionId2);
//        assertFalse(session2Cart.isEmpty(), "Корзина в сессии 2 не должна быть пустой");
//        assertEquals(1, session2Cart.size(), "Корзина в сессии 2 должна содержать 1 товар");
//        assertEquals(2, session2Cart.getFirst().getCount(),
//                "Количество товара в сессии 2 равно 2 - это результат действий в обеих сессиях");
//    }
//}

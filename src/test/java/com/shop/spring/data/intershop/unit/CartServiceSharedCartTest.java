package com.shop.spring.data.intershop.unit;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.impl.CartServiceImpl;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceSharedCartTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShopMapper shopMapper;

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(itemRepository, shopMapper);
    }

    @Test
    void testCartIsSharedBetweenUsers() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setCount(10);
        item.setPrice(100.0);

        ItemDto itemDto = new ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Тестовый товар");
        itemDto.setCount(1);
        itemDto.setPrice(100.0);

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(shopMapper.toItemDto(item)).thenReturn(itemDto);

        String sessionId1 = "user1-session";
        
        //Добавляем товар в корзину первого пользователя
        StepVerifier.create(cartService.updateCartItemQuantity(sessionId1, "1", ActionType.PLUS))
                .verifyComplete();

        //Содержимое корзины первого пользователя
        StepVerifier.create(cartService.getCartItems(sessionId1))
                .assertNext(user1Cart -> {
                    assertFalse(user1Cart.isEmpty(), "Корзина пользователя 1 должна содержать товары");
                    assertEquals(1, user1Cart.size(), "Корзина пользователя 1 должна содержать 1 товар");
                    assertEquals("1", user1Cart.getFirst().getId(), "ID товара должен быть 1");
                    assertEquals(1, user1Cart.getFirst().getCount(), "Количество товара должно быть 1");
                })
                .verifyComplete();

        //Проверяем общую стоимость корзины первого пользователя
        StepVerifier.create(cartService.getCartTotal(sessionId1))
                .assertNext(user1Total -> 
                    assertEquals(100.0, user1Total, 0.01, "Общая стоимость корзины пользователя 1 должна быть 100.0"))
                .verifyComplete();

        String sessionId2 = "user2-session";
        
        //Проверяем содержимое корзины второго пользователя (она должна быть такой же, так как используем общую корзину)
        StepVerifier.create(cartService.getCartItems(sessionId2))
                .assertNext(user2Cart -> {
                    assertEquals(1, user2Cart.size(), "Корзина пользователя 2 содержит 1 товар, так как используется общая корзина");
                    assertEquals(1, user2Cart.getFirst().getCount(), "Количество товара в корзине пользователя 2 равно 1");
                })
                .verifyComplete();

        //Проверяем общую стоимость корзины второго пользователя
        StepVerifier.create(cartService.getCartTotal(sessionId2))
                .assertNext(user2Total -> 
                    assertEquals(100.0, user2Total, 0.01, "Общая стоимость корзины пользователя 2 равна 100.0"))
                .verifyComplete();

        //Добавляем товар в корзину второго пользователя
        StepVerifier.create(cartService.updateCartItemQuantity(sessionId2, "1", ActionType.PLUS))
                .verifyComplete();

        //Проверяем содержимое корзины первого пользователя после добавления товара вторым пользователем
        StepVerifier.create(cartService.getCartItems(sessionId1))
                .assertNext(user1Cart -> 
                    assertEquals(1, user1Cart.size(), "Корзина пользователя 1 должна содержать 1 товар"))
                .verifyComplete();

        //Проверяем общую стоимость корзины первого пользователя
        StepVerifier.create(cartService.getCartTotal(sessionId1))
                .assertNext(user1Total -> 
                    assertEquals(200.0, user1Total, 0.01, "Стоимость корзины пользователя 1 стала равна 200.0"))
                .verifyComplete();
    }

    @Test
    void testCartIsNotIsolatedBetweenSessions() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setCount(10);
        item.setPrice(100.0);

        ItemDto itemDto = new ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Тестовый товар");
        itemDto.setCount(2);
        itemDto.setPrice(100.0);

        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(shopMapper.toItemDto(item)).thenReturn(itemDto);

        String sessionId1 = "session-1";
        
        //Первый пользователь добавляет товар
        StepVerifier.create(cartService.updateCartItemQuantity(sessionId1, "1", ActionType.PLUS))
                .verifyComplete();

        String sessionId2 = "session-2";
        
        //Второй пользователь добавляет тот же товар
        StepVerifier.create(cartService.updateCartItemQuantity(sessionId2, "1", ActionType.PLUS))
                .verifyComplete();

        //Проверяем содержимое корзины первой сессии
        StepVerifier.create(cartService.getCartItems(sessionId1))
                .assertNext(session1Cart -> {
                    assertFalse(session1Cart.isEmpty(), "Корзина в сессии 1 не должна быть пустой");
                    assertEquals(1, session1Cart.size(), "Корзина в сессии 1 должна содержать 1 товар");
                    assertEquals(2, session1Cart.getFirst().getCount(),
                            "Количество товара в сессии 1 равно 2, так как используется общая корзина");
                })
                .verifyComplete();

        //Проверяем содержимое корзины второй сессии
        StepVerifier.create(cartService.getCartItems(sessionId2))
                .assertNext(session2Cart -> {
                    assertFalse(session2Cart.isEmpty(), "Корзина в сессии 2 не должна быть пустой");
                    assertEquals(1, session2Cart.size(), "Корзина в сессии 2 должна содержать 1 товар");
                    assertEquals(2, session2Cart.getFirst().getCount(),
                            "Количество товара в сессии 2 равно 2, так как используется общая корзина");
                })
                .verifyComplete();
    }
}
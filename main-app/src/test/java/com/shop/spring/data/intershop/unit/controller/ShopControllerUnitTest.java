package com.shop.spring.data.intershop.unit.controller;

import com.shop.main.client.api.DefaultApi;
import com.shop.spring.data.intershop.controller.ShopController;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ShopControllerUnitTest {

    @Mock
    private ShopService shopService;
    
    @Mock
    private DefaultApi defaultApi;

    @Mock
    private Model model;
    
    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private WebSession session;

    private ShopController shopController;

    @BeforeEach
    void setUp() {
        shopController = new ShopController(shopService, defaultApi);
    }

    @Test
    void testIndex() {
        Mono<Void> result = shopController.index(exchange);
        
        assertNotNull(result);
    }

    @Test
    void testGetMainItems() {
        List<List<ItemDto>> items = Collections.singletonList(Collections.emptyList());
        when(shopService.getMainItems(anyString(), any(SortType.class), anyInt(), anyInt()))
                .thenReturn(Mono.just(items));

        Mono<String> result = shopController.getMainItems("", "NO", 6, 1, model);

        assertNotNull(result);
        verify(shopService, times(1)).getMainItems("", SortType.NO, 6, 1);
    }

    @Test
    void testGetItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Test Item");
        when(shopService.getItem(anyString())).thenReturn(Mono.just(itemDto));

        Mono<String> result = shopController.getItem("1", model);

        assertNotNull(result);
        verify(shopService, times(1)).getItem("1");
    }
}
package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ShopControllerCustomOrderTest {


    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ShopService shopService;

    private Item testItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testItem = new Item("1", "Тестовый товар", "Описание товара", "/images/test.jpg", 10, 100.0);
        List<Item> orderItems = List.of(testItem);
        testOrder = new Order("1", orderItems);
    }

    @Test
    void testIndexRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    @Test
    void testGetMainItems() throws Exception {
        List<List<Item>> items = new ArrayList<>();
        items.add(Collections.singletonList(testItem));

        when(shopService.getMainItems(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(items);

        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"));
    }


    @Test
    void testGetCartItems() throws Exception {
        List<Item> cartItems = Collections.singletonList(testItem);
        when(shopService.getCartItems()).thenReturn(cartItems);
        when(shopService.getCartTotal()).thenReturn(100.0);
        when(shopService.isCartEmpty()).thenReturn(false);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("empty"));
    }

    @Test
    void testUpdateCartItemQuantity() throws Exception {
        mockMvc.perform(post("/cart/items/1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
    }

    @Test
    void testGetItem() throws Exception {
        when(shopService.getItem("1")).thenReturn(testItem);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void testUpdateItemQuantity() throws Exception {
        mockMvc.perform(post("/items/1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/1"));
    }


    @Test
    void testGetOrders() throws Exception {
        List<Order> orders = Collections.singletonList(testOrder);
        when(shopService.getOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void testGetOrder() throws Exception {
        when(shopService.getOrder("1")).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1")
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("newOrder"));
    }
}

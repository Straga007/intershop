package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ShopControllerCustomOrderTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShopService shopService;

    private ItemDto testItem;
    private OrderDto testOrder;

    @BeforeEach
    void setUp() {
        testItem = new ItemDto();
        testItem.setId("1");
        testItem.setTitle("Тестовый товар");
        testItem.setDescription("Описание товара");
        testItem.setImage("/images/test.jpg");
        testItem.setCount(10);
        testItem.setPrice(100.0);

        List<ItemDto> orderItems = Collections.singletonList(testItem);

        testOrder = new OrderDto();
        testOrder.setId("1");
        testOrder.setItems(orderItems);
    }

    @Test
    void testIndexRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    @Test
    void testGetMainItems() throws Exception {
        List<List<ItemDto>> items = new ArrayList<>();
        items.add(Collections.singletonList(testItem));

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
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void testGetOrder() throws Exception {
        mockMvc.perform(get("/orders/1")
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("newOrder"));
    }
}

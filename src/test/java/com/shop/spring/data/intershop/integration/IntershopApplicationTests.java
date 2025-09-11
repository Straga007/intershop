package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class IntershopApplicationTests {

    @Autowired
    private ShopService shopService;

    @Test
    void contextLoads() {
    }

    @Test
    void testFindAllItems() {
        List<ItemDto> items = shopService.getMainItems("", SortType.NO, 20, 1).getFirst();
        assertNotNull(items);
        assertEquals(15, items.size());
    }

    @Test
    void testFindItemById() {
        ItemDto item = shopService.getItem("1");
        assertNotNull(item);
        assertEquals("1", item.getId());
        assertEquals("Ноутбук Dell", item.getTitle());
    }

    @Test
    void testFindItemsBySearch() {
        List<ItemDto> items = shopService.getMainItems("ноутбук", SortType.NO, 20, 1).getFirst();
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.getFirst().getTitle().toLowerCase().contains("ноутбук"));
    }

    @Test
    void testFindAllOrders() {
        List<OrderDto> orders = shopService.getOrders();
        assertNotNull(orders);
        assertEquals(5, orders.size());
    }

    @Test
    void testShopServiceGetMainItems() {
        List<List<ItemDto>> items = shopService.getMainItems("", SortType.NO, 10, 1);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.getFirst().size() <= 10);
    }

    @Test
    void testShopServiceGetCartItems() {
        List<ItemDto> items = shopService.getCartItems("test-session");
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testShopServiceGetCartTotal() {
        double total = shopService.getCartTotal("test-session");
        assertEquals(0.0, total, 0.01);
    }

    @Test
    void testShopServiceIsCartEmpty() {
        boolean isEmpty = shopService.isCartEmpty("test-session");
        assertTrue(isEmpty);
    }

    @Test
    void testShopServiceGetOrders() {
        List<OrderDto> orders = shopService.getOrders();
        assertNotNull(orders);
        assertEquals(5, orders.size());
    }
}

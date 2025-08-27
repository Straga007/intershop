package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ShopRepository;
import com.shop.spring.data.intershop.service.ShopService;
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
    private ShopRepository shopRepository;

    @Autowired
    private ShopService shopService;

    @Test
    void contextLoads() {
    }

    @Test
    void testFindAllItems() {
        List<Item> items = shopRepository.findAllItems();
        assertNotNull(items);
        assertEquals(15, items.size());
    }

    @Test
    void testFindItemById() {
        Item item = shopRepository.findItemById("1");
        assertNotNull(item);
        assertEquals("1", item.getId());
        assertEquals("Ноутбук Dell", item.getTitle());
    }

    @Test
    void testFindItemsBySearch() {
        List<Item> items = shopRepository.findItemsBySearch("ноутбук");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.getFirst().getTitle().toLowerCase().contains("ноутбук"));
    }

    @Test
    void testCreateOrder() {
        Item item = new Item("1", "Ноутбук", "Описание", "/images/laptop.jpg", 2, 55000.0);
        new Item("2", "Смартфон", "Описание", "/images/phone.jpg", 1, 35000.0);

        String orderId = shopRepository.createOrder(List.of(item));
        assertNotNull(orderId);
        assertTrue(Long.parseLong(orderId) > 0);
    }

    @Test
    void testFindAllOrders() {
        List<Order> orders = shopRepository.findAllOrders();
        assertNotNull(orders);
        assertEquals(5, orders.size());
    }

    @Test
    void testShopServiceGetMainItems() {
        List<List<Item>> items = shopService.getMainItems("", SortType.NO, 10, 1);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.getFirst().size() <= 10);
    }

    @Test
    void testShopServiceGetCartItems() {
        List<Item> items = shopService.getCartItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testShopServiceGetCartTotal() {
        double total = shopService.getCartTotal();
        assertEquals(0.0, total, 0.01);
    }

    @Test
    void testShopServiceIsCartEmpty() {
        boolean isEmpty = shopService.isCartEmpty();
        assertTrue(isEmpty);
    }

    @Test
    void testShopServiceGetOrders() {
        List<Order> orders = shopService.getOrders();
        assertNotNull(orders);
        assertEquals(5, orders.size());
    }
}

package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
//StepVerifier для тестирования реактивных потоков
class IntershopApplicationTests {

    @Autowired
    private ShopService shopService;

    @Test
    void contextLoads() {
    }

    @Test
    void testFindAllItems() {
        StepVerifier.create(shopService.getMainItems("", SortType.NO, 20, 1))
                .assertNext(tuple -> {
                    List<ItemDto> items = tuple.getFirst();
                    assertNotNull(items);
                    assertFalse(items.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void testFindItemById() {
        StepVerifier.create(shopService.getItem("1"))
                .assertNext(item -> {
                    assertNotNull(item);
                    assertEquals("1", item.getId());
                    assertEquals("Ноутбук Dell", item.getTitle());
                })
                .verifyComplete();
    }

    @Test
    void testFindItemsBySearch() {
        StepVerifier.create(shopService.getMainItems("ноутбук", SortType.NO, 20, 1))
                .assertNext(tuple -> {
                    List<ItemDto> items = tuple.getFirst();
                    assertNotNull(items);
                    assertFalse(items.isEmpty());
                    assertTrue(items.getFirst().getTitle().toLowerCase().contains("ноутбук"));
                })
                .verifyComplete();
    }

    @Test
    void testFindAllOrders() {
        StepVerifier.create(shopService.getOrders("test-session"))
                .assertNext(orders -> {
                    assertNotNull(orders);
                    assertFalse(orders.isEmpty());
                    assertEquals(5, orders.size());
                })
                .verifyComplete();
    }

    @Test
    void testShopServiceGetMainItems() {
        StepVerifier.create(shopService.getMainItems("", SortType.NO, 10, 1))
                .assertNext(tuple -> {
                    List<ItemDto> items = tuple.getFirst();
                    assertNotNull(items);
                    assertFalse(items.isEmpty());
                    assertTrue(items.size() <= 10);
                })
                .verifyComplete();
    }

    @Test
    void testShopServiceGetCartItems() {
        StepVerifier.create(shopService.getCartItems("test-session"))
                .assertNext(items -> {
                    assertNotNull(items);
                    assertTrue(items.isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void testShopServiceGetCartTotal() {
        StepVerifier.create(shopService.getCartTotal("test-session"))
                .assertNext(total -> assertEquals(0.0, total, 0.01))
                .verifyComplete();
    }

    @Test
    void testShopServiceIsCartEmpty() {
        StepVerifier.create(shopService.isCartEmpty("test-session"))
                .assertNext(Assertions::assertTrue)
                .verifyComplete();
    }

    @Test
    void testShopServiceGetOrdersList() {
        StepVerifier.create(shopService.getOrders("test-session"))
                .assertNext(orders -> {
                    assertNotNull(orders);
                    assertEquals(5, orders.size());
                })
                .verifyComplete();
    }
}
package com.shop.spring.data.intershop.unit;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ShopRepository;
import com.shop.spring.data.intershop.service.ShopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopService shopService;

    @Test
    void testGetMainItemsWithSearch() {
        List<Item> mockItems = List.of(
                new Item("1", "Ноутбук", "Описание", "/images/laptop.jpg", 10, 55000.0)
        );
        when(shopRepository.findItemsBySearch("ноутбук")).thenReturn(mockItems);

        List<List<Item>> test = shopService.getMainItems("ноутбук", SortType.NO, 10, 1);

        assertNotNull(test);
        assertFalse(test.isEmpty());
        assertEquals(1, test.getFirst().size());
        assertEquals("Ноутбук", test.getFirst().getFirst().getTitle());

        verify(shopRepository).findItemsBySearch("ноутбук");
    }
}

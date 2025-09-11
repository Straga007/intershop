package com.shop.spring.data.intershop.unit;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.impl.ItemServiceImpl;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShopMapper shopMapper;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, shopMapper);
    }

    @Test
    void testGetMainItemsWithSearch() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Ноутбук");
        item.setDescription("Описание");
        item.setImage("/images/laptop.jpg");
        item.setCount(10);
        item.setPrice(55000.0);

        ItemDto itemDto = new ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Ноутбук");
        itemDto.setDescription("Описание");
        itemDto.setImage("/images/laptop.jpg");
        itemDto.setCount(10);
        itemDto.setPrice(55000.0);

        List<Item> mockItems = List.of(item);
        List<ItemDto> mockItemDtos = List.of(itemDto);

        when(itemRepository.findByTitleOrDescriptionContaining("ноутбук")).thenReturn(mockItems);
        when(shopMapper.toItemDtos(mockItems)).thenReturn(mockItemDtos);

        List<List<ItemDto>> result = itemService.getMainItems("ноутбук", SortType.NO, 10, 1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getFirst().size());
        assertEquals("Ноутбук", result.getFirst().getFirst().getTitle());

        verify(itemRepository).findByTitleOrDescriptionContaining("ноутбук");
        verify(shopMapper).toItemDtos(mockItems);
    }
}

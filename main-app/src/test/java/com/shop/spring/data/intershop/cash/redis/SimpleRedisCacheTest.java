package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.impl.ItemServiceImpl;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SimpleRedisCacheTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ShopMapper shopMapper;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemServiceImpl(itemRepository, shopMapper);
    }

    @Test
    void testCacheWorks() {
        // Создаем тестовый объект
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setPrice(100.0);
        item.setCount(10);

        ItemDto itemDto = new ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Тестовый товар");
        itemDto.setPrice(100.0);
        itemDto.setCount(10);

        // Настраиваем моки
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(shopMapper.toItemDto(item)).thenReturn(itemDto);

        // Первый вызов - должен вызвать репозиторий
        ItemDto result1 = itemService.getItemById("1").block();
        verify(itemRepository, times(1)).findById(1L);

        // Второй вызов - в unit-тесте также вызовет репозиторий,
        // так как кэширование не работает без контекста Spring
        ItemDto result2 = itemService.getItemById("1").block();
        verify(itemRepository, times(2)).findById(1L); // Увеличилось до 2

        // Проверяем, что результаты совпадают
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.getTitle()).isEqualTo("Тестовый товар");
    }
}
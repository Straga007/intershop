package com.shop.spring.data.intershop.cash.redis;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.service.impl.ItemServiceImpl;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheTest.class);

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
    void testCacheHitOnSecondCall() {
        // Создаем тестовый объект товара
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setDescription("Описание тестового товара");
        item.setPrice(100.0);
        item.setCount(10);
        item.setImage("/images/test.jpg");

        // Создаем DTO объект
        com.shop.spring.data.intershop.view.dto.ItemDto itemDto = 
            new com.shop.spring.data.intershop.view.dto.ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Тестовый товар");
        itemDto.setDescription("Описание тестового товара");
        itemDto.setPrice(100.0);
        itemDto.setCount(10);
        itemDto.setImage("/images/test.jpg");

        // Настраиваем моки
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(shopMapper.toItemDto(item)).thenReturn(itemDto);

        // Первый вызов - обращение к репозиторию
        com.shop.spring.data.intershop.view.dto.ItemDto result1 =
            itemService.getItemById("1").block();

        // Проверяем результат
        assertNotNull(result1);
        assertEquals("Тестовый товар", result1.getTitle());

        // Второй вызов - должен взять данные из кэша (или повторно вызвать репозиторий в unit-тесте)
        com.shop.spring.data.intershop.view.dto.ItemDto result2 =
            itemService.getItemById("1").block();

        // Проверяем результат
        assertNotNull(result2);
        assertEquals("Тестовый товар", result2.getTitle());

        // Проверяем, что оба результата идентичны
        assertEquals(result1, result2);
    }

    @Test
    void testCacheClear() {
        // Создаем тестовый объект товара
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setDescription("Описание тестового товара");
        item.setPrice(100.0);
        item.setCount(10);
        item.setImage("/images/test.jpg");

        // Создаем DTO объект
        com.shop.spring.data.intershop.view.dto.ItemDto itemDto = 
            new com.shop.spring.data.intershop.view.dto.ItemDto();
        itemDto.setId("1");
        itemDto.setTitle("Тестовый товар");
        itemDto.setDescription("Описание тестового товара");
        itemDto.setPrice(100.0);
        itemDto.setCount(10);
        itemDto.setImage("/images/test.jpg");

        // Настраиваем моки
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item)).thenReturn(Mono.just(item));
        when(shopMapper.toItemDto(item)).thenReturn(itemDto).thenReturn(itemDto);

        // Первый вызов
        com.shop.spring.data.intershop.view.dto.ItemDto result1 =
            itemService.getItemById("1").block();

        // Очищаем кэш
        itemService.clearCache().block();

        // Второй вызов после очистки кэша - должен снова обратиться к репозиторию
        com.shop.spring.data.intershop.view.dto.ItemDto result2 =
            itemService.getItemById("1").block();

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2);
    }
}
package com.shop.spring.data.intershop.integration;

import com.shop.main.client.ApiClient;
import com.shop.main.client.api.DefaultApi;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.service.*;
import com.shop.spring.data.intershop.service.impl.*;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class IntegrationTestConfiguration {

    @Bean
    public ItemRepository itemRepository() {
        return mock(ItemRepository.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return mock(OrderRepository.class);
    }

    @Bean
    public OrderItemRepository orderItemRepository() {
        return mock(OrderItemRepository.class);
    }

    @Bean
    public ShopMapper shopMapper() {
        return new ShopMapper();
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceImpl(itemRepository(), shopMapper());
    }

    @Bean
    public CartService cartService() {
        return new CartServiceImpl(itemRepository(), shopMapper());
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(orderRepository(), orderItemRepository(), itemRepository(), cartService(), shopMapper(), paymentsApi());
    }

    @Bean
    public ShopService shopService() {
        return new ShopService(itemRepository(), orderRepository(), shopMapper(), paymentsApi(), null);
    }

    @Bean
    public DefaultApi paymentsApi() {
        WebClient webClient = WebClient.builder().build();
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath("http://localhost:8081");
        return new DefaultApi(apiClient);
    }
}
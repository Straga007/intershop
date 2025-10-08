package com.shop.spring.data.intershop.service.impl;

import com.shop.main.client.api.DefaultApi;
import com.shop.main.client.model.PaymentRequest;
import com.shop.main.client.model.BalanceResponse;
import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.repository.ItemRepository;
import com.shop.spring.data.intershop.repository.OrderRepository;
import com.shop.spring.data.intershop.repository.OrderItemRepository;
import com.shop.spring.data.intershop.service.CartService;
import com.shop.spring.data.intershop.service.OrderService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import com.shop.spring.data.intershop.view.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ShopMapper shopMapper;
    private final DefaultApi paymentsApi;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, 
                           ItemRepository itemRepository, CartService cartService, ShopMapper shopMapper,
                           DefaultApi paymentsApi) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.cartService = cartService;
        this.shopMapper = shopMapper;
        this.paymentsApi = paymentsApi;
    }

    @Override
    public Mono<String> createOrder(String sessionId) {
        return cartService.getCartTotal(sessionId)
                .flatMap(total -> {
                    PaymentRequest paymentRequest = new PaymentRequest();
                    paymentRequest.setAmount(total);
                    return paymentsApi.processPayment(paymentRequest);
                })
                .flatMap(response -> {
                    // Если платеж успешен, создаем заказ
                    return createOrderInDatabase(sessionId);
                })
                .onErrorResume(throwable -> {
                    // Если платеж неуспешен, возвращаем ошибку
                    return Mono.error(new RuntimeException("Payment failed: " + throwable.getMessage()));
                });
    }
    
    private Mono<String> createOrderInDatabase(String sessionId) {
        return cartService.getCartItems(sessionId)
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new RuntimeException("Корзина пуста"));
                    }
                    
                    Order order = new Order();

                    return orderRepository.save(order)
                            .flatMap(savedOrder -> {
                                List<Mono<OrderItem>> orderItemMonos = new ArrayList<>();
                                
                                for (ItemDto itemDto : cartItems) {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setItemId(Long.valueOf(itemDto.getId()));
                                    orderItem.setQuantity(itemDto.getCount());
                                    orderItem.setOrderId(savedOrder.getId());
                                    
                                    orderItemMonos.add(orderItemRepository.save(orderItem));
                                }

                                assert savedOrder.getId() != null;
                                return Mono.when(orderItemMonos)
                                        .then(cartService.clearCart(sessionId))
                                        .thenReturn(savedOrder.getId());
                            });
                });
    }

    @Override
    public Mono<List<OrderDto>> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .flatMap(order -> 
                    orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .flatMap(orderItems -> {
                            if (orderItems.isEmpty()) {
                                OrderDto orderDto = new OrderDto();
                                orderDto.setId(order.getId());
                                orderDto.setItems(new ArrayList<>());
                                return Mono.just(orderDto);
                            }
                            
                            // Загружаем товары для каждого OrderItem
                            List<Mono<Item>> itemMonos = orderItems.stream()
                                .map(orderItem -> itemRepository.findById(orderItem.getItemId()))
                                .collect(Collectors.toList());
                            
                            return Mono.zip(itemMonos, itemsArray -> {
                                List<Item> items = new ArrayList<>();
                                for (Object item : itemsArray) {
                                    if (item instanceof Item) {
                                        items.add((Item) item);
                                    }
                                }
                                return items;
                            }).map(items -> {
                                // Создаем OrderDto
                                List<ItemDto> itemDtos = new ArrayList<>();
                                
                                for (int i = 0; i < items.size(); i++) {
                                    Item item = items.get(i);
                                    OrderItem orderItem = orderItems.get(i);
                                    
                                    ItemDto itemDto = shopMapper.toItemDto(item);
                                    itemDto.setCount(orderItem.getQuantity());
                                    itemDtos.add(itemDto);
                                }
                                
                                OrderDto orderDto = new OrderDto();
                                orderDto.setId(order.getId());
                                orderDto.setItems(itemDtos);
                                return orderDto;
                            });
                        })
                )
                .collectList();
    }

    @Override
    public Mono<OrderDto> getOrderById(String id) {
        return orderRepository.findById(id)
                .flatMap(order -> 
                    orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .flatMap(orderItems -> {
                            if (orderItems.isEmpty()) {
                                OrderDto orderDto = new OrderDto();
                                orderDto.setId(order.getId());
                                orderDto.setItems(new ArrayList<>());
                                return Mono.just(orderDto);
                            }
                            
                            // Загружаем товары для каждого OrderItem
                            List<Mono<Item>> itemMonos = orderItems.stream()
                                .map(orderItem -> itemRepository.findById(orderItem.getItemId()))
                                .collect(Collectors.toList());
                            
                            return Mono.zip(itemMonos, itemsArray -> {
                                List<Item> items = new ArrayList<>();
                                for (Object item : itemsArray) {
                                    if (item instanceof Item) {
                                        items.add((Item) item);
                                    }
                                }
                                return items;
                            }).map(items -> {
                                // Создаем OrderDto
                                List<ItemDto> itemDtos = new ArrayList<>();
                                
                                for (int i = 0; i < items.size(); i++) {
                                    Item item = items.get(i);
                                    OrderItem orderItem = orderItems.get(i);
                                    
                                    ItemDto itemDto = shopMapper.toItemDto(item);
                                    itemDto.setCount(orderItem.getQuantity());
                                    itemDtos.add(itemDto);
                                }
                                
                                OrderDto orderDto = new OrderDto();
                                orderDto.setId(order.getId());
                                orderDto.setItems(itemDtos);
                                return orderDto;
                            });
                        })
                );
    }
    
    public Mono<Double> checkBalance() {
        System.out.println("Вызов метода checkBalance в OrderServiceImpl");
        
        if (paymentsApi == null) {
            System.out.println("paymentsApi равен null");
            return Mono.error(new IllegalStateException("paymentsApi is null"));
        }
        
        System.out.println("Вызов paymentsApi.getBalance()");
        System.out.println("ApiClient basePath: " + paymentsApi.getApiClient().getBasePath());
        
        // Добавляем логирование перед вызовом
        System.out.println("Подготовка к вызову getBalance()");
        Mono<com.shop.main.client.model.BalanceResponse> responseMono = paymentsApi.getBalance();
        System.out.println("Получен Mono<BalanceResponse>");
        
        // Добавляем логирование подписки
        System.out.println("Подписываемся на Mono<BalanceResponse>");
        
        return responseMono
                .timeout(Duration.ofSeconds(10))
                .doOnNext(response -> {
                    System.out.println("Получен ответ от сервиса платежей: " + response);
                    System.out.println("Баланс из ответа: " + response.getBalance());
                })
                .map(response -> {
                    Double balance = response.getBalance();
                    System.out.println("Преобразуем ответ в баланс: " + balance);
                    return balance;
                })
                .doOnSuccess(balance -> System.out.println("Успешно получен баланс: " + balance))
                .doOnError(throwable -> {
                    System.out.println("Ошибка при получении баланса: " + throwable.getMessage());
                    System.out.println("Тип ошибки: " + throwable.getClass().getName());
                    throwable.printStackTrace();
                })
                .onErrorResume(throwable -> {
                    System.out.println("Обработка ошибки, возвращаем 0.0");
                    return Mono.just(0.0);
                });
    }
}
package com.shop.spring.data.intershop.controller;

import com.shop.main.client.api.DefaultApi;
import com.shop.spring.data.intershop.model.OrderItem;
import com.shop.spring.data.intershop.model.Paging;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Controller
public class ShopController {

    private final ShopService shopService;
    private final DefaultApi defaultApi;

    public ShopController(ShopService shopService, DefaultApi defaultApi) {
        this.shopService = shopService;
        this.defaultApi = defaultApi;
    }

    // get sessionId or userId
    private Mono<String> getUserId(ServerWebExchange exchange, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return Mono.just(authentication.getName());
        }
        return exchange.getSession().map(WebSession::getId);
    }

    @GetMapping("/")
    public Mono<Void> index(ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
            exchange.getResponse().getHeaders().setLocation(URI.create("/main/items"));
        });
    }

    @GetMapping("/main/items")
    public Mono<String> getMainItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            Model model) {

        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        SortType sortType = SortType.valueOf(sort);
        return shopService.getMainItems(search, sortType, pageSize, pageNumber)
                .doOnNext(items -> {
                    model.addAttribute("items", items);
                    boolean hasNext = items.size() == pageSize;
                    Paging paging = new Paging(pageNumber, pageSize, hasNext, pageNumber > 1);
                    model.addAttribute("paging", paging);
                })
                .thenReturn("main");
    }

    @PostMapping("/main/items/{id}")
    public Mono<Void> updateMainItemQuantity(
            @PathVariable String id,
            ServerWebExchange exchange,
            Authentication authentication) {

        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    assert action != null;
                    ActionType actionType = ActionType.valueOf(action.toUpperCase());

                    return getUserId(exchange, authentication)
                            .flatMap(userId -> shopService.updateMainItemQuantity(userId, id, actionType))
                            .then(Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                exchange.getResponse().getHeaders().setLocation(URI.create("/main/items"));
                            }));
                });
    }

    @GetMapping("/cart/items")
    public Mono<String> getCartItems(Model model, ServerWebExchange exchange, Authentication authentication) {
        return getUserId(exchange, authentication)
                .flatMap(userId -> {
                    Mono<List<ItemDto>> itemsMono = shopService.getCartItems(userId);
                    Mono<Double> totalMono = shopService.getCartTotal(userId);
                    Mono<Boolean> emptyMono = shopService.isCartEmpty(userId);
                    Mono<Double> balanceMono = shopService.checkBalance()
                            .doOnNext(balance -> System.out.println("Баланс в контроллере: " + balance))
                            .onErrorReturn(0.0);

                    return Mono.zip(itemsMono, totalMono, emptyMono, balanceMono)
                            .doOnNext(tuple -> {
                                model.addAttribute("items", tuple.getT1());
                                model.addAttribute("total", tuple.getT2());
                                model.addAttribute("empty", tuple.getT3());
                                model.addAttribute("balance", tuple.getT4());
                            })
                            .thenReturn("cart");
                });
    }

    @PostMapping("/cart/items/{id}")
    public Mono<Void> updateCartItemQuantity(
            @PathVariable String id,
            ServerWebExchange exchange,
            Authentication authentication) {

        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    assert action != null;
                    ActionType actionType = ActionType.valueOf(action.toUpperCase());

                    return getUserId(exchange, authentication)
                            .flatMap(userId -> shopService.updateCartItemQuantity(userId, id, actionType))
                            .then(Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                exchange.getResponse().getHeaders().setLocation(URI.create("/cart/items"));
                            }));
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable String id, Model model) {
        return shopService.getItem(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    @PostMapping("/items/{id}")
    public Mono<Void> updateItemQuantity(
            @PathVariable String id,
            ServerWebExchange exchange,
            Authentication authentication) {

        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    assert action != null;
                    ActionType actionType = ActionType.valueOf(action.toUpperCase());

                    return getUserId(exchange, authentication)
                            .flatMap(userId -> shopService.updateItemQuantity(userId, id, actionType))
                            .then(Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                exchange.getResponse().getHeaders().setLocation(URI.create("/items/" + id));
                            }));
                });
    }

    @PostMapping("/buy")
    public Mono<Void> buy(ServerWebExchange exchange, Authentication authentication) {
        return getUserId(exchange, authentication)
                .flatMap(shopService::buy)
                .flatMap(orderId -> {
                    if (orderId != null) {
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                        exchange.getResponse().getHeaders().setLocation(URI.create("/orders/" + orderId + "?newOrder=true"));
                        return Mono.empty();
                    } else {
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                        exchange.getResponse().getHeaders().setLocation(URI.create("/cart/items"));
                        return Mono.empty();
                    }
                });
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model, ServerWebExchange exchange, Authentication authentication) {
        return getUserId(exchange, authentication)
                .flatMap(shopService::getOrders)
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model) {

        return shopService.getOrder(id)
                .doOnNext(orderDto -> {
                    model.addAttribute("order", orderDto);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }

    @GetMapping("/test/orders")
    public Mono<ResponseEntity<List<OrderDto>>> getTestOrders() {
        return shopService.getOrders("test-session")
                .map(orders -> ResponseEntity.ok(orders));
    }

    @GetMapping("/test/orders/{orderId}/items")
    public ResponseEntity<Flux<OrderItem>> getTestOrderItems(@PathVariable String orderId) {
        return ResponseEntity.ok(shopService.getOrderItems(orderId));
    }

    @GetMapping("/test/cart/items")
    public Mono<ResponseEntity<List<ItemDto>>> getTestCartItems() {
        return shopService.getCartItems("test-session")
                .map(ResponseEntity::ok);
    }

    @PostMapping("/test/cart/items/{id}")
    public Mono<ResponseEntity<Void>> updateTestCartItemQuantity(
            @PathVariable String id,
            @RequestParam String action) {
        
        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        return shopService.updateCartItemQuantity("test-session", id, actionType)
                .then(Mono.fromCallable(() -> {
                    System.out.println("Обновлено количество товара в корзине: itemId=" + id + ", action=" + action);
                    return ResponseEntity.ok().build();
                }));
    }
    
    @GetMapping("/api/balance")
    public Mono<ResponseEntity<Double>> getBalance() {
        return shopService.checkBalance()
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Ошибка баланса в контроллере: ", error))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PostMapping("/test/buy")
    public Mono<ResponseEntity<String>> testBuy() {
        return shopService.buy("test-session")
                .map(orderId -> ResponseEntity.ok("Order created with ID: " + orderId))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("Cart is empty")))
                .doOnError(error -> {
                    log.error("Ошибка при оформлении заказа: ", error);
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating order: " + error.getMessage());
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error"));
    }
}
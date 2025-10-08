package com.shop.spring.data.intershop.controller;

import com.shop.spring.data.intershop.model.Paging;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Controller
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    // get sessionId
    private Mono<String> getSessionId(ServerWebExchange exchange) {
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
                    boolean hasNext = !items.isEmpty() && items.getFirst().size() == pageSize;
                    Paging paging = new Paging(pageNumber, pageSize, hasNext, pageNumber > 1);
                    model.addAttribute("paging", paging);
                })
                .thenReturn("main");
    }

    @PostMapping("/main/items/{id}")
    public Mono<Void> updateMainItemQuantity(
            @PathVariable String id,
            ServerWebExchange exchange) {

        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    assert action != null;
                    ActionType actionType = ActionType.valueOf(action.toUpperCase());

                    return getSessionId(exchange)
                            .flatMap(sessionId -> shopService.updateMainItemQuantity(sessionId, id, actionType))
                            .then(Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                exchange.getResponse().getHeaders().setLocation(URI.create("/main/items"));
                            }));
                });
    }

    @GetMapping("/cart/items")
    public Mono<String> getCartItems(Model model, ServerWebExchange exchange) {
        return getSessionId(exchange)
                .flatMap(sessionId -> {
                    Mono<List<ItemDto>> itemsMono = shopService.getCartItems(sessionId);
                    Mono<Double> totalMono = shopService.getCartTotal(sessionId);
                    Mono<Boolean> emptyMono = shopService.isCartEmpty(sessionId);
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
            ServerWebExchange exchange) {

        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    assert action != null;
                    ActionType actionType = ActionType.valueOf(action.toUpperCase());

                    return getSessionId(exchange)
                            .flatMap(sessionId -> shopService.updateCartItemQuantity(sessionId, id, actionType))
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
            ServerWebExchange exchange) {

        return exchange.getFormData()
                .flatMap(formData -> {
                    String action = formData.getFirst("action");
                    assert action != null;
                    ActionType actionType = ActionType.valueOf(action.toUpperCase());

                    return getSessionId(exchange)
                            .flatMap(sessionId -> shopService.updateItemQuantity(sessionId, id, actionType))
                            .then(Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                exchange.getResponse().getHeaders().setLocation(URI.create("/items/" + id));
                            }));
                });
    }

    @PostMapping("/buy")
    public Mono<Void> buy(ServerWebExchange exchange) {
        return getSessionId(exchange)
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
    public Mono<String> getOrders(Model model, ServerWebExchange exchange) {
        return getSessionId(exchange)
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
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }
    
    @GetMapping("/api/balance")
    public Mono<ResponseEntity<Double>> getBalance() {
        System.out.println("Вызов метода getBalance в контроллере");
        return shopService.checkBalance()
                .map(balance -> {
                    System.out.println("Получен баланс в контроллере: " + balance);
                    return ResponseEntity.ok(balance);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
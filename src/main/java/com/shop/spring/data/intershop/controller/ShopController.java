package com.shop.spring.data.intershop.controller;

import com.shop.spring.data.intershop.model.Paging;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.service.ShopService;
import com.shop.spring.data.intershop.view.dto.ItemDto;
import com.shop.spring.data.intershop.view.dto.OrderDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    // get sessionId
    private String getSessionId(HttpSession session) {
        return session.getId();
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getMainItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            Model model,
            HttpSession session) {

        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        SortType sortType = SortType.valueOf(sort);
        List<List<ItemDto>> items = shopService.getMainItems(search, sortType, pageSize, pageNumber);
        model.addAttribute("items", items);

        boolean hasNext = !items.isEmpty() && items.getFirst().size() == pageSize;
        Paging paging = new Paging(pageNumber, pageSize, hasNext, pageNumber > 1);
        model.addAttribute("paging", paging);

        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String updateMainItemQuantity(
            @PathVariable String id,
            @RequestParam String action,
            HttpSession session) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        shopService.updateMainItemQuantity(getSessionId(session), id, actionType);

        return "redirect:/main/items";
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model, HttpSession session) {
        String sessionId = getSessionId(session);
        model.addAttribute("items", shopService.getCartItems(sessionId));
        model.addAttribute("total", shopService.getCartTotal(sessionId));
        model.addAttribute("empty", shopService.isCartEmpty(sessionId));

        return "cart";
    }

    @PostMapping("/cart/items/{id}")
    public String updateCartItemQuantity(
            @PathVariable String id,
            @RequestParam String action,
            HttpSession session) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        shopService.updateCartItemQuantity(getSessionId(session), id, actionType);

        return "redirect:/cart/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable String id, Model model, HttpSession session) {
        model.addAttribute("item", shopService.getItem(id));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateItemQuantity(
            @PathVariable String id,
            @RequestParam String action,
            HttpSession session) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        shopService.updateItemQuantity(getSessionId(session), id, actionType);

        return "redirect:/items/" + id;
    }

    @PostMapping("/buy")
    public String buy(HttpSession session) {
        String orderId = shopService.buy(getSessionId(session));

        if (orderId != null) {
            return "redirect:/orders/" + orderId + "?newOrder=true";
        } else {
            return "redirect:/cart/items";
        }
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        model.addAttribute("orders", shopService.getOrders());

        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model) {

        OrderDto order = shopService.getOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);

        return "order";
    }
}

package com.shop.spring.data.intershop.controller;

import com.shop.spring.data.intershop.model.Item;
import com.shop.spring.data.intershop.model.Order;
import com.shop.spring.data.intershop.model.Paging;
import com.shop.spring.data.intershop.model.enums.ActionType;
import com.shop.spring.data.intershop.model.enums.SortType;
import com.shop.spring.data.intershop.service.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
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
            Model model) {

        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        SortType sortType = SortType.valueOf(sort);
        List<List<Item>> items = shopService.getMainItems(search, sortType, pageSize, pageNumber);
        model.addAttribute("items", items);

        boolean hasNext = !items.isEmpty() && items.getFirst().size() == pageSize;
        Paging paging = new Paging(pageNumber, pageSize, hasNext, pageNumber > 1);
        model.addAttribute("paging", paging);

        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String updateMainItemQuantity(
            @PathVariable String id,
            @RequestParam String action) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        shopService.updateMainItemQuantity(id, actionType);

        return "redirect:/main/items";
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        model.addAttribute("items", shopService.getCartItems());
        model.addAttribute("total", shopService.getCartTotal());
        model.addAttribute("empty", shopService.isCartEmpty());

        return "cart";
    }

    @PostMapping("/cart/items/{id}")
    public String updateCartItemQuantity(
            @PathVariable String id,
            @RequestParam String action) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        shopService.updateCartItemQuantity(id, actionType);

        return "redirect:/cart/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable String id, Model model) {
        model.addAttribute("item", shopService.getItem(id));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateItemQuantity(
            @PathVariable String id,
            @RequestParam String action) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        shopService.updateItemQuantity(id, actionType);

        return "redirect:/items/" + id;
    }

    @PostMapping("/buy")
    public String buy() {
        String orderId = shopService.buy();

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

        Order order = shopService.getOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);

        return "order";
    }
}

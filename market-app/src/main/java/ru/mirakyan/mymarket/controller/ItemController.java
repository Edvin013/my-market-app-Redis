package ru.mirakyan.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.enums.SortType;
import ru.mirakyan.mymarket.security.SecurityUtils;
import ru.mirakyan.mymarket.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model) {

        return itemService.getItems(search, sort, pageNumber, pageSize)
                .zipWith(itemService.getPagingInfo(search, pageNumber, pageSize))
                .zipWith(SecurityUtils.isAuthenticated())
                .zipWith(SecurityUtils.getCurrentUsername().defaultIfEmpty(""))
                .doOnNext(tuple -> {
                    var itemsAndPaging = tuple.getT1().getT1();
                    model.addAttribute("items", itemsAndPaging.getT1());
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("paging", itemsAndPaging.getT2());
                    model.addAttribute("isAuthenticated", tuple.getT1().getT2());
                    model.addAttribute("username", tuple.getT2());
                })
                .thenReturn("items");
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> updateCartFromItems(ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(formData -> {
            Long id = Long.parseLong(formData.getFirst("id"));
            ItemAction action = ItemAction.valueOf(formData.getFirst("action"));
            String search = formData.getFirst("search");
            SortType sort = SortType.valueOf(formData.getOrDefault("sort", java.util.List.of("NO")).get(0));
            int pageNumber = Integer.parseInt(formData.getOrDefault("pageNumber", java.util.List.of("1")).get(0));
            int pageSize = Integer.parseInt(formData.getOrDefault("pageSize", java.util.List.of("5")).get(0));

            return itemService.updateCartItem(id, action)
                    .then(Mono.fromCallable(() -> {
                        StringBuilder redirectUrl = new StringBuilder("redirect:/items?");
                        if (search != null && !search.isEmpty()) {
                            redirectUrl.append("search=").append(search).append("&");
                        }
                        redirectUrl.append("sort=").append(sort)
                                   .append("&pageNumber=").append(pageNumber)
                                   .append("&pageSize=").append(pageSize);
                        return redirectUrl.toString();
                    }));
        });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return itemService.getItemById(id)
                .zipWith(SecurityUtils.isAuthenticated())
                .zipWith(SecurityUtils.getCurrentUsername().defaultIfEmpty(""))
                .doOnNext(tuple -> {
                    model.addAttribute("item", tuple.getT1().getT1());
                    model.addAttribute("isAuthenticated", tuple.getT1().getT2());
                    model.addAttribute("username", tuple.getT2());
                })
                .thenReturn("item");
    }

    @PostMapping(value = "/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> updateCartFromItem(
            @PathVariable Long id,
            ServerWebExchange exchange,
            Model model) {

        return exchange.getFormData().flatMap(formData -> {
            ItemAction action = ItemAction.valueOf(formData.getFirst("action"));
            return itemService.updateCartItem(id, action)
                    .then(itemService.getItemById(id))
                    .doOnNext(item -> model.addAttribute("item", item))
                    .thenReturn("item");
        });
    }
}

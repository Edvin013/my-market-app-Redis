package ru.mirakyan.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.enums.SortType;
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
                .doOnNext(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("paging", tuple.getT2());
                })
                .thenReturn("items");
    }

    @PostMapping("/items")
    public Mono<String> updateCartFromItems(
            @RequestParam Long id,
            @RequestParam ItemAction action,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {

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
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return itemService.getItemById(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    @PostMapping("/items/{id}")
    public Mono<String> updateCartFromItem(
            @PathVariable Long id,
            @RequestParam ItemAction action,
            Model model) {

        return itemService.updateCartItem(id, action)
                .then(itemService.getItemById(id))
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }
}

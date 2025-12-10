package ru.mirakyan.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Mono<String> getCartItems(Model model) {
        return cartService.getCartItems()
                .collectList()
                .zipWith(cartService.getTotalPrice())
                .doOnNext(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                })
                .thenReturn("cart");
    }

    @PostMapping("/items")
    public Mono<String> updateCartItem(
            @RequestParam Long id,
            @RequestParam ItemAction action,
            Model model) {

        return cartService.updateCartItem(id, action)
                .then(cartService.getCartItems().collectList())
                .zipWith(cartService.getTotalPrice())
                .doOnNext(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                })
                .thenReturn("cart");
    }
}


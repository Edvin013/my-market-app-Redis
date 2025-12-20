package ru.mirakyan.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.service.CartService;
import ru.mirakyan.mymarket.service.PaymentClient;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final PaymentClient paymentClient;

    @GetMapping("/items")
    public Mono<String> getCartItems(Model model) {
        return cartService.getCartItems()
                .collectList()
                .zipWith(cartService.getTotalPrice())
                .zipWith(paymentClient.getBalance())
                .zipWith(paymentClient.isServiceAvailable())
                .doOnNext(tuple -> {
                    var itemsAndTotalAndBalance = tuple.getT1();
                    var itemsAndTotal = itemsAndTotalAndBalance.getT1();
                    var items = itemsAndTotal.getT1();
                    var total = itemsAndTotal.getT2();
                    var balance = itemsAndTotalAndBalance.getT2();
                    var paymentServiceAvailable = tuple.getT2();

                    model.addAttribute("items", items);
                    model.addAttribute("total", total);
                    model.addAttribute("balance", balance);
                    model.addAttribute("paymentServiceAvailable", paymentServiceAvailable);

                    boolean canCheckout = paymentServiceAvailable && balance >= total;
                    model.addAttribute("canCheckout", canCheckout);
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
                .zipWith(paymentClient.getBalance())
                .zipWith(paymentClient.isServiceAvailable())
                .doOnNext(tuple -> {
                    var itemsAndTotalAndBalance = tuple.getT1();
                    var itemsAndTotal = itemsAndTotalAndBalance.getT1();
                    var items = itemsAndTotal.getT1();
                    var total = itemsAndTotal.getT2();
                    var balance = itemsAndTotalAndBalance.getT2();
                    var paymentServiceAvailable = tuple.getT2();

                    model.addAttribute("items", items);
                    model.addAttribute("total", total);
                    model.addAttribute("balance", balance);
                    model.addAttribute("paymentServiceAvailable", paymentServiceAvailable);

                    boolean canCheckout = paymentServiceAvailable && balance >= total;
                    model.addAttribute("canCheckout", canCheckout);
                })
                .thenReturn("cart");
    }
}


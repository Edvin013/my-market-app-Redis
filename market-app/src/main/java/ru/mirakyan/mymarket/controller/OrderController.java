package ru.mirakyan.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.security.SecurityUtils;
import ru.mirakyan.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/orders")
    public Mono<String> getAllOrders(Model model) {
        return orderService.getAllOrders()
                .collectList()
                .zipWith(SecurityUtils.getCurrentUsername().defaultIfEmpty(""))
                .doOnNext(tuple -> {
                    model.addAttribute("orders", tuple.getT1());
                    model.addAttribute("username", tuple.getT2());
                })
                .thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model) {

        return orderService.getOrderById(id)
                .zipWith(SecurityUtils.getCurrentUsername().defaultIfEmpty(""))
                .doOnNext(tuple -> {
                    model.addAttribute("order", tuple.getT1());
                    model.addAttribute("newOrder", newOrder);
                    model.addAttribute("username", tuple.getT2());
                })
                .thenReturn("order");
    }

    @PostMapping("/buy")
    public Mono<String> createOrder() {
        return orderService.createOrder()
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
    }
}


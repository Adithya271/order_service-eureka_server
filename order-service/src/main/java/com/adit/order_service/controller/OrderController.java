package com.adit.order_service.controller;


import com.adit.order_service.model.*;
import com.adit.order_service.service.OrderService;
import com.adit.order_service.vo.ResponseTemplate;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Order> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Order create(@RequestBody Order order) {
        return service.create(order);
    }

   
    @GetMapping("/product/{id}")
    public ResponseTemplate getOrderWithProduk(@PathVariable Long id) {
        return service.getOrderWithProduct(id);
    }
}
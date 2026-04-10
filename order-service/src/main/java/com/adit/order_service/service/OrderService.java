package com.adit.order_service.service;

import com.adit.order_service.model.*;
import com.adit.order_service.repository.OrderRepository;
import com.adit.order_service.vo.Product;
import com.adit.order_service.vo.ResponseTemplate;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository repository, RabbitTemplate rabbitTemplate) {
       this.repository = repository;
       this.rabbitTemplate = rabbitTemplate;
    }

    public List<Order> getAll() {
        return repository.findAll();
    }

    public Order getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    //logic total
    public Order create(Order order) {

        Product product = restTemplate.getForObject(
                "http://localhost:8081/api/product/" + order.getProductId(),
                Product.class
        );

        if (product != null) {
            double total = product.getHarga() * order.getJumlah();
            order.setTotal(total);
        }

        Order saved = repository.save(order);
        
        // Kirim notifikasi ke RabbitMQ
        String message = "Order baru masuk! ID: " + saved.getId()
                + ", Product ID: " + saved.getProductId()
                + ", Jumlah: " + saved.getJumlah()
                + ", Total: " + saved.getTotal();
        rabbitTemplate.convertAndSend("order.notification", message);
        System.out.println("Pesan terkirim ke RabbitMQ: " + message);

        return saved;
    }

    public ResponseTemplate getOrderWithProduct(Long id) {

        Order order = repository.findById(id).orElse(null);

        if (order == null) return null;

        Product product = restTemplate.getForObject(
                "http://localhost:8081/api/product/" + order.getProductId(),
                Product.class
        );

        ResponseTemplate response = new ResponseTemplate();
        response.setOrder(order);
        response.setProduct(product);

        return response;
    }
}
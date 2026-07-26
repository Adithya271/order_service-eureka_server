package com.adit.order_service.service;

import com.adit.order_service.model.*;
import com.adit.order_service.repository.OrderRepository;
import com.adit.order_service.vo.Product;
import com.adit.order_service.vo.ResponseTemplate;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final RabbitTemplate rabbitTemplate;

    @Value("${PRODUCT_SERVICE_URL:http://localhost:8083}")
    private String productServiceUrl;

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

    public Order create(Order order) {
        // Ambil token dari request
        String token = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // Kirim token ke product-service
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Product> productResponse = new RestTemplate().exchange(
            productServiceUrl + "/api/product/" + order.getProductId(),
            HttpMethod.GET,
            entity,
            Product.class
        );

        Product product = productResponse.getBody();
        if (product != null) {
            double total = product.getHarga() * order.getJumlah();
            order.setTotal(total);
        }

        Order saved = repository.save(order);
        
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

        // Ambil token dari request header
        String token = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        System.out.println("Token dikirim ke product-service: " + token.substring(0, 20) + "...");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Product> response = new RestTemplate().exchange(
            productServiceUrl + "/api/product/" + order.getProductId(),
            HttpMethod.GET,
            entity,
            Product.class
        );

        ResponseTemplate result = new ResponseTemplate();
        result.setOrder(order);
        result.setProduct(response.getBody());

        return result;
    }
}
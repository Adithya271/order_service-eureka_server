package com.adit.order_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity (name="orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private int jumlah;
    private double total;
}

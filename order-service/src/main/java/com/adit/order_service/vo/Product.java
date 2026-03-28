package com.adit.order_service.vo;

import lombok.Data;

@Data
public class Product {
    private Long id;
    private String nama;
    private String satuan;
    private double harga;
}

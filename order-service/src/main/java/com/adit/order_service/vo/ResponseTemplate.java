package com.adit.order_service.vo;

import com.adit.order_service.model.Order;

import lombok.Data;

@Data
public class ResponseTemplate {
   Order order;
   Product product;
}

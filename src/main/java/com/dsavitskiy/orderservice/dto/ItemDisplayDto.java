package com.dsavitskiy.orderservice.dto;


import java.math.BigDecimal;

public record ItemDisplayDto(
     Long id,
     String name,
     BigDecimal price) {
    }

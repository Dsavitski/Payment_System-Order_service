package com.dsavitskiy.orderservice.dto;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDisplayDto(
    Long id,
    UUID userId,
    String status,
    BigDecimal totalPrice,
    boolean deleted,
    List<OrderItemDisplayDto> items){

}

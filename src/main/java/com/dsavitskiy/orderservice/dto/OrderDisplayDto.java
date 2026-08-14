package com.dsavitskiy.orderservice.dto;


import com.dsavitskiy.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDisplayDto(
    Long id,
    UUID userId,
    OrderStatus status,
    BigDecimal totalPrice,
    boolean deleted,
    List<OrderItemDisplayDto> items){

}

package com.dsavitskiy.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateDto(
    @NotEmpty(message = "Order must contain items")
    List<OrderItemCreateDto> items) {
}

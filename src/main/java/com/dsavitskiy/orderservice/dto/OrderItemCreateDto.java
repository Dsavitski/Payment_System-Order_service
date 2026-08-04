package com.dsavitskiy.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemCreateDto(
    @NotNull(message = "Item id is obligatory")
    Long itemId,
    @Positive(message = "Quantity must be > 0")
    @NotNull(message = "Quantity is obligatory")
    Integer quantity) {
}

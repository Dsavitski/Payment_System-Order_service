package com.dsavitskiy.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemCreateDto(
    @NotBlank(message = "Name is obligatory")
    String name,
    @NotNull(message = "Price is obligatory")
    @Positive(message = "Price must be positive")
    BigDecimal price){
}

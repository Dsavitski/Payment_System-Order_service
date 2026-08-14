package com.dsavitskiy.orderservice.dto;

public record OrderResponseDto(
    OrderDisplayDto order,
    UserDisplayDto user
) {
}
package com.dsavitskiy.orderservice.dto;

import java.util.UUID;

public record UserDisplayDto(
    UUID id,
    String name,
    String surname,
    String email
) {
}
package com.dsavitskiy.orderservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserDisplayDto(
    UUID id,
    String name,
    String surname,
    String email,
    LocalDate birthDate,
    boolean active
) {
}
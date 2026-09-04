package com.dsavitskiy.orderservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCreatedEvent(
        UUID paymentId,
        UUID userId,
        Long orderId,
        BigDecimal paymentAmount,
        String status,
        Instant timestamp) {

}
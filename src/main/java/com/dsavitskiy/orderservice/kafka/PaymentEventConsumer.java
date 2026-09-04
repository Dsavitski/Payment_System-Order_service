package com.dsavitskiy.orderservice.kafka;

import com.dsavitskiy.orderservice.event.PaymentCreatedEvent;
import com.dsavitskiy.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "${kafka.topic.payment-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentCreatedEvent(PaymentCreatedEvent event) {
        log.info("Received PaymentCreated event: paymentId={}, orderId={}, status={}",
                event.paymentId(), event.orderId(), event.status());

        try {
            if ("SUCCESS".equals(event.status())) {
                log.info("Payment is successfull");
                orderService.markOrderAsPaid(event.orderId());
            } else {
                log.info("Payment is cancelled");
                orderService.markOrderAsPaymentFailed(event.orderId());
            }
        } catch (Exception e) {
            log.error("Error handling event for orderId={}: {}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }
}
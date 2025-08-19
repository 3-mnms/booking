package com.mnms.kafka.booking.listener;

import com.mnms.booking.service.BookingCommandService;
import com.mnms.kafka.booking.dto.PaymentSuccessEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final BookingCommandService bookingCommandService;

    @KafkaListener(topics = "${app.kafka.topic.payment-event}", groupId = "booking-service-group",  containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentSuccess(PaymentSuccessEventDTO event) {
        log.info("Received payment success event: {}", event);
        bookingCommandService.confirmTicket(event.getReservationNumber());
    }
}
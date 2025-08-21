package com.mnms.booking.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mnms.booking.kafka.dto.PaymentSuccessEventDTO;
import com.mnms.booking.service.BookingCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final BookingCommandService bookingCommandService;

    @KafkaListener(topics = "${app.kafka.topic.payment-event}", groupId = "booking-service-group")
    public void consumePaymentSuccess(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        PaymentSuccessEventDTO event = objectMapper.readValue(message, PaymentSuccessEventDTO.class);
        bookingCommandService.confirmTicket(event.getReservationNumber(), event.isSuccess());
    }
}
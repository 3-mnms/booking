package com.mnms.booking.kafka.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.kafka.dto.PaymentSuccessEventDTO;
import com.mnms.booking.service.BookingCommandService;
import com.mnms.booking.service.TransferCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentListener {

    private final BookingCommandService bookingCommandService;
    private final TransferCompletionService transferCompletionService;

    @KafkaListener(topics = "${app.kafka.topic.payment-event}", groupId = "booking-service-group")
    public void consumePaymentSuccess(String message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        PaymentSuccessEventDTO event = objectMapper.readValue(message, PaymentSuccessEventDTO.class);
        String method = event.getMethod();

        switch (method) {
            case "payment" -> bookingCommandService.confirmTicket(event.getReservationNumber(), event.isSuccess());
            case "cancel" -> bookingCommandService.cancelBooking(event.getReservationNumber(), event.isSuccess());
            case "transfer" -> transferCompletionService.updateOthersTicket(event.getReservationNumber(), event.isSuccess());
            default -> throw new BusinessException(ErrorCode.PAYMENT_RESPONSE_ERROR);
        }
    }
}
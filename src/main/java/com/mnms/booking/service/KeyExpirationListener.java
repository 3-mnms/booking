package com.mnms.booking.service;

import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeyExpirationListener implements MessageListener {

    private final TicketRepository ticketRepository;

    ///  ticket 가예매 스케줄러
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        if (key.startsWith("TEMP_RESERVATION:")) {
            String reservationNumber = key.substring("TEMP_RESERVATION:".length());
            
            ticketRepository.findByReservationNumber(reservationNumber)
                    .filter(t -> t.getReservationStatus() == ReservationStatus.TEMP_RESERVED)
                    .ifPresent(ticketRepository::delete);
        }
    }
}
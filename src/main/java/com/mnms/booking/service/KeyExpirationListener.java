package com.mnms.booking.service;

import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeyExpirationListener implements MessageListener {

    private final TicketRepository ticketRepository;
    private final WaitingService waitingService;
    private final QrCodeRepository qrCodeRepository;

    ///  ticket 가예매 스케줄러
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        log.info("onMessage key : {}", key);
        if (key.startsWith("TEMP_RESERVATION:")) {
            String reservationNumber = key.substring("TEMP_RESERVATION:".length());
            log.info("onMessage reservationNumber : {}", reservationNumber);

            // 예매열 삭제
            Ticket reservation = loadReservationMeta(reservationNumber);
            log.info("onMessage reservation : {}", reservation);

            waitingService.userExitBookingPage(
                    reservation.getFestival().getFestivalId(),
                    reservation.getPerformanceDate(),
                    String.valueOf(reservation.getUserId())
            );

            // qr 있으면 삭제
            List<QrCode> qrCodes = qrCodeRepository.findByTicketId(reservation.getId());
            if (!qrCodes.isEmpty()) {
                qrCodeRepository.deleteAll(qrCodes);
            }

            // 티켓 가예매 삭제
            ticketRepository.findByReservationNumber(reservationNumber)
                    .filter(t -> t.getReservationStatus() == ReservationStatus.TEMP_RESERVED
                            || t.getReservationStatus() == ReservationStatus.PAYMENT_IN_PROGRESS)
                    .ifPresent(ticketRepository::delete);
        }
    }

    private Ticket loadReservationMeta(String reservationNumber) {
        return ticketRepository.findByReservationNumberWithFestival(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }
}
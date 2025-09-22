package com.mnms.booking.event;

import com.mnms.booking.dto.response.BookingUserResponseDTO;
import com.mnms.booking.service.EmailService;
import com.mnms.booking.service.TempReservationService;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventListener {

    private final UserApiClient userApiClient;
    private final EmailService emailService;
    private final TempReservationService tempReservationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTicketConfirmed(TicketConfirmedEvent event) {
        var ticketDto = event.ticket(); // TicketRequestDTO

        try {
            BookingUserResponseDTO user = userApiClient.getUserInfoById(ticketDto.getUserId());
            tempReservationService.deleteTempReservation(ticketDto.getReservationNumber());
            emailService.sendTicketConfirmationEmail(ticketDto, user);
            log.info("티켓 확정 후 이메일 전송 완료: reservationNumber={}", ticketDto.getReservationNumber());
        } catch (Exception e) {
            log.error("티켓 확정 후 이메일 전송 실패: reservationNumber={}", ticketDto.getReservationNumber(), e);
        }
    }
}
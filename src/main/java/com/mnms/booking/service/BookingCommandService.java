package com.mnms.booking.service;

import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.entity.*;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.event.TicketConfirmedEvent;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.util.CommonUtils;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingCommandService {
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final CommonUtils commonUtils;
    private final UserApiClient userApiClient;
    private final BookingStatusService bookingStatusService;
    private final TempReservationService tempReservationService;
    private final ApplicationEventPublisher eventPublisher;


    /// 1차: 가예매 - 임시 예약 (2차 예매하기 누르면 실행)
    @Transactional
    public String selectFestivalDate(BookingSelectRequestDTO request, Long userId) {
        Festival festival = bookingStatusService.getFestivalOrThrow(request.getFestivalId());
        LocalDateTime performanceDate = request.getPerformanceDate();

        bookingStatusService.validatePerformanceDate(festival, performanceDate);
        bookingStatusService.validateScheduleExists(festival, performanceDate);
        bookingStatusService.recreateHold(festival, performanceDate, userId); // 가예매 상태인 티켓 모두 삭제
        bookingStatusService.validateUserReservationLimit(userId, request, festival);

        Ticket ticket = Ticket.builder()
                .festival(festival)
                .userId(userId)
                .reservationNumber(commonUtils.generateReservationNumber())
                .selectedTicketCount(request.getSelectedTicketCount())
                .performanceDate(performanceDate)
                .reservationStatus(ReservationStatus.TEMP_RESERVED)
                .build();

        ticketRepository.save(ticket);
        // redis ttl : 1분 설정
        tempReservationService.createTempReservation(ticket);
        return ticket.getReservationNumber();
    }

    /// 2차: 가예매 - 배송 방법 선택 (결제하기 누르면 실행)
    @Transactional
    public void selectFestivalDelivery(BookingSelectDeliveryRequestDTO request, Long userId) {
        Ticket ticket = bookingStatusService.getTicketOrThrow(request.getFestivalId(), userId, request.getReservationNumber());
        TicketType type = bookingStatusService.parseDeliveryMethod(request.getDeliveryMethod());
        ticket.setDeliveryMethod(type);
        if(TicketType.PAPER.equals(type)){
            ticket.setAddress(request.getAddress());
            ticket.setDeliveryDate(bookingStatusService.calculateDeliveryDate(ticket, type));
        }
        ticketRepository.save(ticket);

        // redis ttl : 5분 설정
        tempReservationService.refreshTempReservation(ticket.getReservationNumber(), 5);
    }

    /// 3차: 가예매 - 예약 - QR생성 (마지막 결제하기 눌렀을 때 실행)
    @Transactional
    public void reserveTicket(BookingRequestDTO request, Long userId) {
        Festival festival = bookingStatusService.getFestivalOrThrow(request.getFestivalId());
        Long selectedTicketCount = ticketRepository.findTicketCountByReservationNumber(request.getReservationNumber());

        bookingStatusService.validateCapacity(festival, request, selectedTicketCount);

        Ticket ticket = bookingStatusService.getTicketByReservationNumberOrThrow(request.getReservationNumber());
        ticket.setReservationStatus(ReservationStatus.PAYMENT_IN_PROGRESS);
        bookingStatusService.ensureDeliveryStepCompleted(ticket);

        bookingStatusService.regenerateQrCodes(ticket, userId, festival);

        ticketRepository.save(ticket);

        // redis ttl : 3분 설정 (예매 완료하면 ttl 바로 완료됨)
        tempReservationService.refreshTempReservation(ticket.getReservationNumber(), 3);
    }


    /// 최종 완료 - status 변경 (payment에 kafka 메시지 구독)
    @Transactional
    public void confirmTicket(String reservationNumber, boolean paymentStatus) {
        Ticket ticket = bookingStatusService.getTicketByReservationNumberOrThrow(reservationNumber);
        ReservationStatus newStatus = bookingStatusService.determineReservationStatus(paymentStatus);

        // 결제 상태 변경
        bookingStatusService.updateTicketStatusIfNecessary(ticket, newStatus);
        eventPublisher.publishEvent(new TicketConfirmedEvent(TicketRequestDTO.fromEntity(ticket)));
    }

    ///  예매 취소
    @Transactional
    public void cancelBooking(String reservationNumber, boolean paymentStatus) {
        Ticket ticket = ticketRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        ReservationStatus status = paymentStatus
                ? ReservationStatus.CANCELED
                : ticket.getReservationStatus();

        if(ticket.getReservationStatus() == ReservationStatus.CANCELED){
            throw new BusinessException(ErrorCode.TICKET_ALREADY_CANCELED);
        }
        if (ticket.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.TICKET_FAIL_CANCEL);
        }
        // qr 삭제 + ticket 상태 변경
        ticket.getQrCodes().clear();
        ticket.setReservationStatus(status);
        ticketRepository.save(ticket);
    }

    // 예매 완료 확인
    public ReservationStatus checkStatus(String reservationNumber) {
        return ticketRepository.findReservationStatusByRN(reservationNumber);
    }
}
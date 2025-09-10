package com.mnms.booking.service;

import  com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.*;
import com.mnms.booking.entity.*;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.util.CommonUtils;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /// 1차: 가예매 - 임시 예약
    @Transactional
    public String selectFestivalDate(BookingSelectRequestDTO request, Long userId) {
        Festival festival = bookingStatusService.getFestivalOrThrow(request.getFestivalId());
        LocalDateTime performanceDate = request.getPerformanceDate();

        bookingStatusService.validatePerformanceDate(festival, performanceDate);
        bookingStatusService.validateScheduleExists(festival, performanceDate);
        bookingStatusService.validateUserReservationLimit(userId, request, festival);

        Ticket ticket = Ticket.builder()
                .festival(festival)
                .userId(userId)
                .reservationNumber(commonUtils.generateReservationNumber())
                .selectedTicketCount(request.getSelectedTicketCount())
                .performanceDate(performanceDate)
                .reservationStatus(ReservationStatus.TEMP_RESERVED)
                .reservationDate(LocalDate.now())
                .build();

        ticketRepository.save(ticket);

        // TTL 스케줄링: 일정 시간 지나면 자동 삭제
        bookingStatusService.scheduleTempReservationExpiration(ticket.getReservationNumber());

        return ticket.getReservationNumber();
    }

    /// 2차: 가예매 - 배송 방법 선택
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
    }

    /// 3차: 가예매 - 예약 - QR생성
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
    }

    /// 최종 완료 - status 변경 (payment에 kafka 메시지 구독)
    @Transactional
    public void confirmTicket(String reservationNumber, boolean paymentStatus) {
        Ticket ticket = bookingStatusService.getTicketByReservationNumberOrThrow(reservationNumber);
        ReservationStatus newStatus = bookingStatusService.determineReservationStatus(paymentStatus);

        // 결제 상태 변경
        bookingStatusService.updateTicketStatusIfNecessary(ticket, newStatus);

        BookingUserResponseDTO user = userApiClient.getUserInfoById(ticket.getUserId());
        emailService.sendTicketConfirmationEmail(ticket, user);

        // websocket
        bookingStatusService.notifyTicketStatus(ticket, newStatus);
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

    // websocket 손실 방지 확인
    public ReservationStatus checkStatus(String reservationNumber) {
        return ticketRepository.findReservationStatusByReservationNumber(reservationNumber);
    }
}
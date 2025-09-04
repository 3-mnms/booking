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
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.util.CommonUtils;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingCommandService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int TEMP_RESERVATION_TTL_MINUTES = 30; // 가예매 유지 시간

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;

    private final QrCodeService qrCodeService;
    private final EmailService emailService;

    private final ThreadPoolTaskScheduler scheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final CommonUtils commonUtils;
    private final UserApiClient userApiClient;

    /// 1차: 가예매 - 임시 예약
    @Transactional
    public String selectFestivalDate(BookingSelectRequestDTO request, Long userId) {
        Festival festival = getFestivalOrThrow(request.getFestivalId());
        LocalDateTime performanceDate = request.getPerformanceDate();

        validatePerformanceDate(festival, performanceDate);
        validateScheduleExists(festival, performanceDate);
        validateUserReservationLimit(userId, request, festival);

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
        scheduleTempReservationExpiration(ticket.getReservationNumber());

        return ticket.getReservationNumber();
    }

    /// 2차: 가예매 - 배송 방법 선택
    @Transactional
    public void selectFestivalDelivery(BookingSelectDeliveryRequestDTO request, Long userId) {
        Ticket ticket = getTicketOrThrow(request.getFestivalId(), userId, request.getReservationNumber());
        TicketType type = parseDeliveryMethod(request.getDeliveryMethod());
        ticket.setDeliveryMethod(type);
        if(TicketType.PAPER.equals(type)){
            ticket.setAddress(request.getAddress());
            ticket.setDeliveryDate(calculateDeliveryDate(ticket, type));
        }
        ticketRepository.save(ticket);
    }

    /// 3차: 가예매 - 예약 - QR생성
    @Transactional
    public void reserveTicket(BookingRequestDTO request, Long userId) {
        Festival festival = getFestivalOrThrow(request.getFestivalId());
        Long selectedTicketCount = ticketRepository.findSelectedTicketCountByReservationNumber(request.getReservationNumber());

        validateCapacity(festival, request, selectedTicketCount);

        Ticket ticket = getTicketByReservationNumberOrThrow(request.getReservationNumber());
        ticket.setReservationStatus(ReservationStatus.PAYMENT_IN_PROGRESS);
        ensureDeliveryStepCompleted(ticket);

        regenerateQrCodes(ticket, userId, festival);

        ticketRepository.save(ticket);
    }

    private void validateCapacity(Festival festival, BookingRequestDTO request, Long selectedTicketCount) {
        int totalCount = ticketRepository.getTotalSelectedTicketCount(
                request.getFestivalId(),
                request.getPerformanceDate(),
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.PAYMENT_IN_PROGRESS)
        );

        if (totalCount + selectedTicketCount > festival.getAvailableNOP()) {
            throw new BusinessException(ErrorCode.FESTIVAL_LIMIT_AVALIABLE_PEOPLE);
        }
    }

    private void regenerateQrCodes(Ticket ticket, Long userId, Festival festival) {
        ticket.getQrCodes().clear();
        ticket.getQrCodes().addAll(
                IntStream.range(0, ticket.getSelectedTicketCount())
                        .mapToObj(i -> createAndSaveQrCode(userId, festival, ticket))
                        .toList()
        );
    }

    /// 최종 완료 - status 변경 (payment에 kafka 메시지 구독)
    @Transactional
    public void confirmTicket(String reservationNumber, boolean paymentStatus) {
        Ticket ticket = getTicketByReservationNumberOrThrow(reservationNumber);
        ReservationStatus newStatus = determineReservationStatus(paymentStatus);

        // 결제 상태 변경
        updateTicketStatusIfNecessary(ticket, newStatus);

        BookingUserResponseDTO user = userApiClient.getUserInfoById(ticket.getUserId());
        emailService.sendTicketConfirmationEmail(ticket, user);

        // websocket
        notifyTicketStatus(ticket, newStatus);
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

    /// schedule 점검
    private void scheduleTempReservationExpiration(String reservationNumber) {
        scheduler.schedule(() -> {
            ticketRepository.findByReservationNumber(reservationNumber)
                    .filter(t -> t.getReservationStatus() == ReservationStatus.TEMP_RESERVED)
                    .ifPresent(ticketRepository::delete);
        }, Instant.now().plus(TEMP_RESERVATION_TTL_MINUTES, ChronoUnit.MINUTES));
    }

    /// 검증
    private void validatePerformanceDate(Festival festival, LocalDateTime performanceDate) {
        LocalDate date = performanceDate.toLocalDate();
        if (date.isBefore(festival.getFdfrom()) || date.isAfter(festival.getFdto())) {
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }
    }

    private void validateScheduleExists(Festival festival, LocalDateTime performanceDate) {
        String dayOfWeek = performanceDate.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase();

        boolean exists = festival.getSchedules().stream()
                .anyMatch(s -> s.getDayOfWeek().equalsIgnoreCase(dayOfWeek)
                        && LocalTime.parse(s.getTime(), TIME_FORMATTER).equals(performanceDate.toLocalTime()));

        if (!exists) throw new BusinessException(ErrorCode.FESTIVAL_INVALID_TIME);
    }

    private void validateUserReservationLimit(Long userId, BookingSelectRequestDTO request, Festival festival) {
        int selectTicketCount = request.getSelectedTicketCount();
        if (festival.getMaxPurchase() < selectTicketCount){
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }

        LocalDateTime startDate = request.getPerformanceDate();
        LocalDateTime endDate = startDate.plusSeconds(1);

        Long alreadyReserved = ticketRepository.sumSelectedTicketCount(
                userId, festival.getFestivalId(), startDate, endDate);
        if (alreadyReserved + selectTicketCount > festival.getMaxPurchase()) {
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }
    }

    private void ensureDeliveryStepCompleted(Ticket ticket) {
        if (ticket.getDeliveryMethod() == null && ticket.getDeliveryDate() == null) {
            throw new BusinessException(ErrorCode.TICKET_DELIVERY_NOT_COMPLETED);
        }
    }

    /// 기타
    private ReservationStatus determineReservationStatus(boolean paymentStatus) {
        return paymentStatus ? ReservationStatus.CONFIRMED : ReservationStatus.CANCELED;
    }

    private void updateTicketStatusIfNecessary(Ticket ticket, ReservationStatus newStatus) {
        if (ticket.getReservationStatus() != ReservationStatus.CONFIRMED
                && ticket.getReservationStatus() != ReservationStatus.CANCELED) {
            ticket.setReservationStatus(newStatus);
            ticketRepository.save(ticket);
        }
    }

    // websocket
    private void notifyTicketStatus(Ticket ticket, ReservationStatus status) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(ticket.getUserId()),
                "/queue/ticket-status",
                new TicketStatusResponseDTO(ticket.getReservationNumber(), status)
        );
    }

    private Festival getFestivalOrThrow(String festivalId) {
        return festivalRepository.findByFestivalId(festivalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    private Ticket getTicketOrThrow(String festivalId, Long userId, String reservationNumber) {
        return ticketRepository.findByFestivalIdAndUserIdAndReservationNumber(festivalId, userId, reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private Ticket getTicketByReservationNumberOrThrow(String reservationNumber) {
        return ticketRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private TicketType parseDeliveryMethod(String method) {
        if (method == null) throw new BusinessException(ErrorCode.FESTIVAL_DELIVERY_INVALID);
        try {
            return TicketType.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TICKET_INVALID_DELIVERY_METHOD);
        }
    }

    /// deliveryDate 생성
    private LocalDateTime calculateDeliveryDate(Ticket ticket, TicketType deliveryMethod) {
        if (ticket.getPerformanceDate() == null) throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);

        if (deliveryMethod == TicketType.PAPER) {
            LocalDateTime deliveryDate = ticket.getPerformanceDate().minusDays(14);
            return deliveryDate.isAfter(LocalDateTime.now())
                    ? deliveryDate
                    : LocalDateTime.now().plusDays(1);
        }
        return null; // 모바일 티켓
    }

    /// Qr정보 생성
    private QrCode createAndSaveQrCode(Long userId, Festival festival, Ticket ticket) {
        // 중복 없는 QR 코드 ID 생성
        String qrCodeId;
        do {
            qrCodeId = qrCodeService.generateQrCodeId();
        } while (qrCodeRepository.existsByQrCodeId(qrCodeId));

        QrCode qrCode = QrResponseDTO.create(userId, qrCodeId, festival, ticket).toEntity();
        qrCodeRepository.save(qrCode);
        return qrCode;
    }

    public ReservationStatus checkStatus(String reservationNumber) {
        return ticketRepository.findReservationStatusByReservationNumber(reservationNumber);
    }
}
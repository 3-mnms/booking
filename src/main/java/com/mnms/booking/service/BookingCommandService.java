package com.mnms.booking.service;

import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.*;
import com.mnms.booking.entity.*;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingCommandService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;
    private final QrCodeService qrCodeService;

    /// 1차: 임시 예약
    @Transactional
    public String selectFestivalDate(BookingSelectRequestDTO request, Long userId) {
        Festival festival = getFestivalOrThrow(request.getFestivalId());
        LocalDateTime performanceDate = request.getPerformanceDate();

        validatePerformanceDate(festival, performanceDate);
        validateScheduleExists(festival, performanceDate);
        validateTicketAvailability(festival, request.getSelectedTicketCount());
        validateUserReservationLimit(userId, request, festival);

        Ticket ticket = Ticket.builder()
                .festival(festival)
                .userId(userId)
                .reservationNumber(generateReservationNumber())
                .selectedTicketCount(request.getSelectedTicketCount())
                .performanceDate(performanceDate)
                .reservationStatus(ReservationStatus.TEMP_RESERVED)
                .reservationDate(LocalDate.now())
                .build();

        ticketRepository.save(ticket);
        return ticket.getReservationNumber();
    }

    /// 2차: 배송 방법 선택
    @Transactional
    public void selectFestivalDelivery(BookingSelectDeliveryRequestDTO request, Long userId) {
        Ticket ticket = getTicketOrThrow(request.getFestivalId(), userId, request.getReservationNumber());
        TicketType type = parseDeliveryMethod(request.getDeliveryMethod());

        ticket.setDeliveryMethod(type);
        ticket.setDeliveryDate(calculateDeliveryDate(ticket, type));
        ticketRepository.save(ticket);
    }

    /// 3차: 최종 예약 - QR생성
    @Transactional
    public BookingResponseDTO reserveTicket(BookingRequestDTO request, Long userId) {
        Festival festival = getFestivalOrThrow(request.getFestivalId());
        Ticket ticket = getTicketByReservationNumberOrThrow(request.getReservationNumber());

        ensureDeliveryStepCompleted(ticket);

        QrCode qrCode = createAndSaveQrCode(userId, festival, ticket);

        ticket.setQrCode(qrCode);
        ticketRepository.save(ticket);
        return BookingResponseDTO.fromEntity(ticket);
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

    private void validateTicketAvailability(Festival festival, int selectedTicketCount) {
        if (festival.getAvailableNOP() < selectedTicketCount) {
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }
    }

    private void validateUserReservationLimit(Long userId, BookingSelectRequestDTO request, Festival festival) {
        int alreadyReserved = ticketRepository.countByUserIdAndFestivalIdAndPerformanceDate(
                userId, festival.getId(), request.getPerformanceDate());
        if (alreadyReserved + request.getSelectedTicketCount() > festival.getMaxPurchase()) {
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }
    }

    private void ensureDeliveryStepCompleted(Ticket ticket) {
        if (ticket.getDeliveryMethod() == null || ticket.getDeliveryDate() == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_DELIVERY_NOT_COMPLETED);
        }
    }

    /// 기타
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
        String qrCodeId = qrCodeService.generateQrCodeId();
        if (qrCodeId == null || qrCodeId.isEmpty()) {
            throw new BusinessException(ErrorCode.QR_CODE_ID_GENERATION_FAILED);
        }
        QrCode qrCode = QrResponseDTO.create(userId, qrCodeId, festival, ticket).toEntity();
        qrCodeRepository.save(qrCode);
        return qrCode;
    }

    ///  reservation number 랜덤 생성
    private String generateReservationNumber() {
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "T" + uuidPart;
    }
}
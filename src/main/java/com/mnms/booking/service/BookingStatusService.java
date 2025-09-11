package com.mnms.booking.service;

import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.QrResponseDTO;
import com.mnms.booking.dto.response.TicketStatusResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingStatusService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int TEMP_RESERVATION_TTL_MINUTES = 20; // 가예매 유지 시간

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;
    private final QrCodeService qrCodeService;
    private final ThreadPoolTaskScheduler scheduler;
    private final SimpMessagingTemplate messagingTemplate;
//
//    /// schedule 점검
//    public void scheduleTempReservationExpiration(String reservationNumber) {
//        scheduler.schedule(() -> {
//            ticketRepository.findByReservationNumber(reservationNumber)
//                    .filter(t -> t.getReservationStatus() == ReservationStatus.TEMP_RESERVED)
//                    .ifPresent(ticketRepository::delete);
//        }, Instant.now().plus(TEMP_RESERVATION_TTL_MINUTES, ChronoUnit.MINUTES));
//    }
//
//    /// redis를 통해 schedule 점검
//    private final RedisTemplate<String, Object> redisTemplate;
//    private static final String PREFIX = "TEMP_RESERVATION:"; // Key prefix
//    private static final long TTL_MINUTES = 5;
//
//    public void createTempReservation(Ticket ticket) {
//        String key = PREFIX + ticket.getReservationNumber();
//        redisTemplate.opsForValue().set(
//                key,
//                ticket,
//                TTL_MINUTES,
//                TimeUnit.MINUTES
//        );
//    }
//
//    public Optional<Ticket> getTempReservation(String reservationNumber) {
//        String key = PREFIX + reservationNumber;
//        Ticket ticket = (Ticket) redisTemplate.opsForValue().get(key);
//        return Optional.ofNullable(ticket);
//    }
//
//    public void deleteTempReservation(String reservationNumber) {
//        redisTemplate.delete(PREFIX + reservationNumber);
//    }


    /// 검증
    public void validateCapacity(Festival festival, BookingRequestDTO request, Long selectedTicketCount) {
        int totalCount = ticketRepository.getTotalSelectedTicketCount(
                request.getFestivalId(),
                request.getPerformanceDate(),
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.PAYMENT_IN_PROGRESS)
        );

        if (totalCount + selectedTicketCount > festival.getAvailableNOP()) {
            throw new BusinessException(ErrorCode.FESTIVAL_LIMIT_AVAILABLE_PEOPLE);
        }
    }

    public void validatePerformanceDate(Festival festival, LocalDateTime performanceDate) {
        LocalDate date = performanceDate.toLocalDate();
        if (date.isBefore(festival.getFdfrom()) || date.isAfter(festival.getFdto())) {
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }
    }

    public void validateScheduleExists(Festival festival, LocalDateTime performanceDate) {
        String dayOfWeek = performanceDate.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase();

        boolean exists = festival.getSchedules().stream()
                .anyMatch(s -> s.getDayOfWeek().equalsIgnoreCase(dayOfWeek)
                        && LocalTime.parse(s.getTime(), TIME_FORMATTER).equals(performanceDate.toLocalTime()));

        if (!exists) throw new BusinessException(ErrorCode.FESTIVAL_INVALID_TIME);
    }

    public void validateUserReservationLimit(Long userId, BookingSelectRequestDTO request, Festival festival) {
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

    public void ensureDeliveryStepCompleted(Ticket ticket) {
        if (ticket.getDeliveryMethod() == null && ticket.getDeliveryDate() == null) {
            throw new BusinessException(ErrorCode.TICKET_DELIVERY_NOT_COMPLETED);
        }
    }

    /// 기타
    public void regenerateQrCodes(Ticket ticket, Long userId, Festival festival) {
        ticket.getQrCodes().clear();
        ticket.getQrCodes().addAll(
                IntStream.range(0, ticket.getSelectedTicketCount())
                        .mapToObj(i -> createAndSaveQrCode(userId, festival, ticket))
                        .toList()
        );
    }

    public ReservationStatus determineReservationStatus(boolean paymentStatus) {
        return paymentStatus ? ReservationStatus.CONFIRMED : ReservationStatus.CANCELED;
    }

    public void updateTicketStatusIfNecessary(Ticket ticket, ReservationStatus newStatus) {
        if (ticket.getReservationStatus() != ReservationStatus.CONFIRMED
                && ticket.getReservationStatus() != ReservationStatus.CANCELED) {
            ticket.setReservationStatus(newStatus);
            ticketRepository.save(ticket);
        }
    }

    // websocket
    public void notifyTicketStatus(Ticket ticket, ReservationStatus status) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(ticket.getUserId()),
                "/queue/ticket-status",
                new TicketStatusResponseDTO(ticket.getReservationNumber(), status)
        );
    }

    public Festival getFestivalOrThrow(String festivalId) {
        return festivalRepository.findByFestivalId(festivalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    public Ticket getTicketOrThrow(String festivalId, Long userId, String reservationNumber) {
        return ticketRepository.findByIdAndReservationNumber(festivalId, userId, reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    public Ticket getTicketByReservationNumberOrThrow(String reservationNumber) {
        return ticketRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    public TicketType parseDeliveryMethod(String method) {
        if (method == null) throw new BusinessException(ErrorCode.FESTIVAL_DELIVERY_INVALID);
        try {
            return TicketType.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TICKET_INVALID_DELIVERY_METHOD);
        }
    }

    /// deliveryDate 생성
    public LocalDateTime calculateDeliveryDate(Ticket ticket, TicketType deliveryMethod) {
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

    ///  예매 시도 시, 가예매 상태 모두 지우기
    public void recreateHold(Festival festival, LocalDateTime performanceDate, Long userId) {
        List<Ticket> tempReservedTickets = ticketRepository.findTempReservedTickets(festival.getId(), performanceDate, userId, ReservationStatus.TEMP_RESERVED);
        ticketRepository.deleteAllInBatch(tempReservedTickets);
    }
}

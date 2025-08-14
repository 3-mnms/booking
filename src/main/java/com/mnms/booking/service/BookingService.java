package com.mnms.booking.service;

import com.mnms.booking.dto.request.FestivalSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.FestivalSelectRequestDTO;
import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.dto.response.*;
import com.mnms.booking.entity.*;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.ScheduleRepository;
import com.mnms.booking.repository.TicketRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;
    private final QrCodeService qrCodeService;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public FestivalDetailResponseDTO getFestivalDetail(@Valid FestivalSelectRequestDTO request) {
        // 페스티벌 조회
        Festival festival = festivalRepository.findByFestivalId(request.getFestivalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        LocalDateTime performanceDate = request.getPerformanceDate();
        LocalDate selectDate = performanceDate.toLocalDate();
        LocalTime selectTime = performanceDate.toLocalTime();

        // 날짜 범위 검증
        if (selectDate.isBefore(festival.getFdfrom()) || selectDate.isAfter(festival.getFdto())) {
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }

        // 스케줄 찾기
        String dayOfWeek = selectDate.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase();

        Schedule matchedSchedule = festival.getSchedules().stream()
                .filter(s -> s.getDayOfWeek().equalsIgnoreCase(dayOfWeek) &&
                        LocalTime.parse(s.getTime(), DateTimeFormatter.ofPattern("HH:mm")).equals(selectTime))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_INVALID_TIME));

        List<Schedule> schedules = scheduleRepository.findByFestivalId(festival.getFestivalId());
        List<ScheduleResponseDTO> scheduleDTOs = schedules.stream()
                .map(s -> ScheduleResponseDTO.builder()
                        .scheduleId(s.getScheduleId())
                        .dayOfWeek(s.getDayOfWeek())
                        .time(s.getTime())
                        .build())
                .toList();

        // 4. DTO 반환
        return FestivalDetailResponseDTO.builder()
                .fname(festival.getFname())
                .ticketPrice(festival.getTicketPrice())
                .posterFile(festival.getPosterFile())
                .maxPurchase(festival.getMaxPurchase())
                .performanceDate(request.getPerformanceDate())
                .schedules(scheduleDTOs)
                .build();
    }

    // 가예매 상황 : 1차 저장
    @Transactional
    public void selectFestivalDate(FestivalSelectRequestDTO request, Long userId) {
        Festival festival = festivalRepository.findByFestivalId(request.getFestivalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 요청한 날짜가 해당 페스티벌의 공연 일정에 포함되어 있는지 확인
        LocalDateTime performanceDate = request.getPerformanceDate();
        LocalDate selectDate = performanceDate.toLocalDate();
        LocalTime selectTime = performanceDate.toLocalTime();

        if(selectDate.isBefore(festival.getFdfrom()) || selectDate.isAfter(festival.getFdto())){
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }

        String dayOfWeek = selectDate.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase();
        boolean isValidSchedule = festival.getSchedules().stream()
                .anyMatch(s ->
                        s.getDayOfWeek().equalsIgnoreCase(dayOfWeek) &&
                                LocalTime.parse(s.getTime()).equals(selectTime)
                );

        if (!isValidSchedule) {
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_TIME);
        }

        if(festival.getAvailableNOP() < request.getSelectedTicketCount()){
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }

        // 페스티벌, 유저, 선택 날짜, 시간 저장
        Ticket ticket = Ticket.builder()
                .festival(festival)
                .userId(userId)
                .selectedTicketCount(request.getSelectedTicketCount())
                .performanceDate(performanceDate)
                .reservationStatus(ReservationStatus.TEMP_RESERVED)
                .build();
        ticketRepository.save(ticket);
    }

    // 가예매 상황 : 2차 조회
    @Transactional(readOnly = true)
    public FestivalBookingDetailResponseDTO getFestivalBookingDetail(@Valid FestivalSelectRequestDTO request, Long userId) {
        // 공연 정보 조회
        Festival festival = festivalRepository.findByFestivalId(request.getFestivalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        LocalDateTime performanceDate = request.getPerformanceDate();
        LocalDate date = performanceDate.toLocalDate();
        LocalTime time = performanceDate.toLocalTime();

        // 2. 예매 티켓 조회 (해당 유저, 해당 공연 시간)
        Ticket ticket = ticketRepository.findByFestivalIdAndUserIdAndPerformanceDate(request.getFestivalId(), userId, performanceDate)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        // 4. DTO 생성
        return FestivalBookingDetailResponseDTO.builder()
                .festivalName(festival.getFname())
                .performanceDate(date)
                .performanceTime(time)
                .posterFile(festival.getPosterFile())
                .ticketCount(ticket.getSelectedTicketCount())
                .ticketPrice(festival.getTicketPrice())
                .build();
    }

    // 가예매 상황 : 2차 저장
    @Transactional
    public void selectFestivalDelivery(FestivalSelectDeliveryRequestDTO request, Long userId) {
        // 1. 해당 티켓 조회
        Ticket ticket = ticketRepository.findByFestivalIdAndUserIdAndPerformanceDate(
                request.getFestivalId(),
                userId,
                request.getPerformanceDate()
        ).orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        TicketType type;
        try {
            type = TicketType.valueOf(request.getDeliveryMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TICKET_INVALID_DELIVERY_METHOD);
        }

        // 티켓 엔티티에 수령 방법 설정
        ticket.setDeliveryMethod(type);

        // 저장
        ticketRepository.save(ticket);
    }


    @Transactional
    public TicketResponseDTO reserveTicket(TicketRequestDTO request, Long userId) {
        Festival festival = festivalRepository.findByFestivalId(request.getFestivalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 사용자 예약 티켓 매수, 지류티켓 확인
        validateReservation(userId, request, festival);
        LocalDateTime deliveryDate = calculateDeliveryDate(request.getDeliveryMethod(), request);

        // Ticket 저장 (reservation number 자동 래덤 생성됨)
        Ticket ticket = buildTicket(request, userId, festival, deliveryDate);

        // QR 생성 (Ticket 포함)
        QrCode qrCode = createAndSaveQrCode(userId, festival, ticket);

        // Ticket에 QR 세팅 후 저장
        ticket.setQrCode(qrCode);
        ticketRepository.save(ticket);

        // 응답
        return TicketResponseDTO.fromEntity(ticket);
    }

    private void validateReservation(Long userId, TicketRequestDTO request, Festival festival) {

        int alreadyReservedCount = ticketRepository.countByUserIdAndFestivalId(userId, festival.getId());
        int reservedCount = request.getSelectedTicketCount() + alreadyReservedCount;

        // 티켓 개수 초과
        if ( reservedCount > festival.getMaxPurchase()) {
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }

        // 배송 방법 유효성 검사
        if (request.getDeliveryMethod() == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_DELIVERY_INVALID);
        }
    }

    // deliveryDate 생성
    private LocalDateTime calculateDeliveryDate(TicketType deliveryMethod, TicketRequestDTO request) {
        // 지류 티켓일 경우
        if (request.getPerformanceDate() == null) {
                throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }
        if (deliveryMethod == TicketType.PAPER) {
            LocalDateTime deliveryDate = request.getPerformanceDate().minusDays(14);
            return deliveryDate.isAfter(LocalDateTime.now())
                    ? deliveryDate
                    : LocalDateTime.now().plusDays(1);
        }
        // 모바일 티켓일 경우
        return null;
    }

    // Qr정보 생성
    private QrCode createAndSaveQrCode(Long userId, Festival festival, Ticket ticket) {
        try {
            String qrCodeId = qrCodeService.generateQrCodeId();
            if (qrCodeId == null || qrCodeId.isEmpty()) {
                throw new BusinessException(ErrorCode.QR_CODE_ID_GENERATION_FAILED);
            }
            QrResponseDTO qrResponse = QrResponseDTO.create(userId, qrCodeId, festival, ticket);
            QrCode qrCode = qrResponse.toEntity();
            qrCodeRepository.save(qrCode);
            return qrCode;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.QR_CODE_SAVE_FAILED);
        }
    }

    // Ticket 저장
    private Ticket buildTicket(TicketRequestDTO request, Long userId, Festival festival,
                               LocalDateTime deliveryDate) { // , QrCode qrCode
        return Ticket.builder()
                .reservationNumber(generateReservationNumber())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .deliveryMethod(request.getDeliveryMethod())
                .reservationDate(LocalDate.now())
                .deliveryDate(deliveryDate)
                .performanceDate(request.getPerformanceDate())
                .selectedTicketCount(request.getSelectedTicketCount())
                .festival(festival)
                .userId(userId)
                .build();
    }

    // reservation number 랜덤 생성
    private String generateReservationNumber() {
        // T + UUID를 앞 8자리
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "T" + uuidPart;
    }
}
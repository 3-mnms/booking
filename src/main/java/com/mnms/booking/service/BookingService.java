package com.mnms.booking.service;

import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.dto.response.QrResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.TicketType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;
    private final QrCodeService qrCodeService;

    @Transactional
    public TicketResponseDTO reserveTicket(TicketRequestDTO request, Long userId) {
        // 페스티벌 정보 조회
        Festival festival = festivalRepository.findById(request.getFestivalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 사용자 예약 티켓 매수, 지류티켓 확인
        validateReservation(userId, request, festival);
        LocalDate deliveryDate = calculateDeliveryDate(request.getDeliveryMethod(), request);

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
        if ( reservedCount > festival.getMaxTicketsPerUser()) {
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }

        // 배송 방법 유효성 검사
        if (request.getDeliveryMethod() == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_DELIVERY_INVALID);
        }
    }

    // deliveryDate 생성
    private LocalDate calculateDeliveryDate(TicketType deliveryMethod, TicketRequestDTO request) {
        // 지류 티켓일 경우
        if (request.getPerformanceDate() == null || request.getPerformanceTime() == null) {
                throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }
        if (deliveryMethod == TicketType.PAPER) {
            LocalDate deliveryDate = request.getPerformanceDate().minusDays(14);
            return deliveryDate.isAfter(LocalDate.now())
                    ? deliveryDate
                    : LocalDate.now().plusDays(1);
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
                               LocalDate deliveryDate) { // , QrCode qrCode
        return Ticket.builder()
                .reservationNumber(generateReservationNumber())
                .reservationStatus(true)
                .deliveryMethod(request.getDeliveryMethod())
                .reservationDate(LocalDate.now())
                .deliveryDate(deliveryDate)
                .performanceDate(request.getPerformanceDate())
                .performanceTime(request.getPerformanceTime())
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
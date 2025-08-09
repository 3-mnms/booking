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

        // 해당 사용자가 이미 같은 페스티벌에 티켓을 예약했는지 확인
        validateReservation(userId, request);
        LocalDate deliveryDate = calculateDeliveryDate(request.getDeliveryMethod(), festival);

        // Qr정보 생성
        QrCode qrCode = createAndSaveQrCode(userId, festival);

        // Ticket 저장 (reservation number 자동 래덤 생성됨)
        Ticket ticket = buildTicket(request, userId, festival, deliveryDate, qrCode);
        ticketRepository.save(ticket);

        // 응답
        return TicketResponseDTO.fromEntity(ticket);
    }

    private void validateReservation(Long userId,TicketRequestDTO request) {

        if (ticketRepository.existsByUserIdAndFestivalId(userId, request.getFestivalId())) {
            throw new BusinessException(ErrorCode.TICKET_ALREADY_RESERVED);
        }
        if (request.getDeliveryMethod() == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_DELIVERY_INVALID);
        }
    }

    // deliveryDate 생성
    private LocalDate calculateDeliveryDate(TicketType deliveryMethod, Festival festival) {
        // 지류 티켓일 경우
        if (festival.getPrfpdfrom() == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }

        if (deliveryMethod == TicketType.PAPER && festival.getPrfpdto() != null) {
            LocalDate deliveryDate = festival.getPrfpdfrom().minusDays(14);
            return deliveryDate.isAfter(LocalDate.now())
                    ? deliveryDate
                    : LocalDate.now().plusDays(1);
        }

        // 모바일 티켓일 경우
        return null;
    }

    // Qr정보 생성
    private QrCode createAndSaveQrCode(Long userId, Festival festival) {
        try {
            String qrCodeId = qrCodeService.generateQrCodeId();
            if (qrCodeId == null || qrCodeId.isEmpty()) {
                throw new BusinessException(ErrorCode.QR_CODE_ID_GENERATION_FAILED);
            }
            QrResponseDTO qrResponse = QrResponseDTO.create(userId, qrCodeId, festival);
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
                               LocalDate deliveryDate, QrCode qrCode) {
        return Ticket.builder()
                .reservationNumber(generateReservationNumber())
                .reservationStatus(true)
                .deliveryMethod(request.getDeliveryMethod())
                .reservationDate(LocalDate.now())
                .deliveryDate(deliveryDate)
                .festival(festival)
                .qrCode(qrCode)
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
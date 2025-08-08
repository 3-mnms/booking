package com.mnms.booking.service;

import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.dto.response.QrResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.TicketType;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                .orElseThrow(() -> new IllegalArgumentException("Festival not found"));

        // 티켓 택배 날짜
        LocalDateTime deliveryDate = calculateDeliveryDate(request.getDeliveryMethod(), festival);

        // Qr정보 생성
        String qrCodeId = qrCodeService.generateQrCodeId();
        QrResponseDTO qrResponse = QrResponseDTO.create(userId, qrCodeId, festival);
        QrCode qrCode = qrResponse.toEntity();
        qrCodeRepository.save(qrCode);

        // Ticket 저장 (reservation number 자동 래덤 생성됨)
        Ticket ticket = Ticket.builder()
                .reservationNumber(generateReservationNumber())
                .reservationStatus(true)
                .deliveryMethod(request.getDeliveryMethod())
                .reservationDate(LocalDateTime.now())
                .deliveryDate(deliveryDate)
                .festival(festival)
                .qrCode(qrCode)
                .userId(userId)
                .build();
        ticketRepository.save(ticket);

        // 응답
        return TicketResponseDTO.fromEntity(ticket);
    }

    // reservation number 랜덤 생성
    private String generateReservationNumber() {
        // T + UUID를 앞 8자리
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "T" + uuidPart;
    }

    // deliverydate 생성
    private LocalDateTime calculateDeliveryDate(TicketType deliveryMethod, Festival festival) {
        // 지류 티켓일 경우
        if (deliveryMethod == TicketType.PAPER && festival.getPrfpdto() != null) {
            return festival.getPrfpdto().minusDays(14);
        }
        // mobile일 경우
        return null;
    }
}
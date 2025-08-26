package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.TicketType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class TicketDetailResponseDTO {

    // ticket
    private Long id;
    private String reservationNumber; // 예매 번호
    private LocalDateTime performanceDate; // 공연 일시
    private TicketType deliveryMethod; // 티켓 수령 방법
    private List<String> qrId;
    private String address;

    // festival
    private String fname; // 공연명
    private String fcltynm; // 장소

    public static TicketDetailResponseDTO fromEntity(Ticket ticket, Festival festival) {
        List<String> qrIds = ticket.getQrCodes().stream()
                .map(QrCode::getQrCodeId)
                .toList();

        return TicketDetailResponseDTO.builder()
                .id(ticket.getId())
                .reservationNumber(ticket.getReservationNumber())
                .performanceDate(ticket.getPerformanceDate())
                .deliveryMethod(ticket.getDeliveryMethod())
                .address(ticket.getAddress())
                .qrId(qrIds)
                .fname(festival.getFname())
                .fcltynm(festival.getFcltynm())
                .build();
    }
}

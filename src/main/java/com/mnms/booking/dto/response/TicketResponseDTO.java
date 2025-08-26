package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TicketType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


// 예매 내역 기본 조회
@Data
@Builder
public class TicketResponseDTO {

    // ticket
    private Long id;
    private String reservationNumber; // 예매 번호
    private LocalDateTime performanceDate; // 공연 일시
    private int selectedTicketCount; // 매수
    private TicketType deliveryMethod; // 티켓 수령 방법
    private LocalDate reservationDate; // 예매를 수행한 날짜
    private ReservationStatus reservationStatus; // 예매상태

    // festival
    private String fname; // 공연명
    private String fcltynm; // 장소

    public static TicketResponseDTO fromEntity(Ticket ticket, Festival festival) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .reservationNumber(ticket.getReservationNumber())
                .performanceDate(ticket.getPerformanceDate())
                .selectedTicketCount(ticket.getSelectedTicketCount())
                .deliveryMethod(ticket.getDeliveryMethod())
                .reservationDate(ticket.getReservationDate())
                .reservationStatus(ticket.getReservationStatus())
                .fname(festival.getFname())
                .fcltynm(festival.getFcltynm())
                .build();
    }
}

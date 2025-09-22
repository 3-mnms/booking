package com.mnms.booking.dto.request;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.TicketType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketRequestDTO {

    private Long userId;
    private String reservationNumber;

    private String fname;      // 페스티벌 이름
    private LocalDateTime performanceDate; // 선택 날짜

    private Festival festival;
    private String festivalFacility;  // 공연장 장소
    private int ticketPrice;
    private int selectedTicketCount;  // 선택 매수
    private TicketType deliveryMethod;

    public static TicketRequestDTO fromEntity(Ticket ticket) {
        return TicketRequestDTO.builder()
                .userId(ticket.getUserId())
                .reservationNumber(ticket.getReservationNumber())
                .fname(ticket.getFestival().getFname())
                .performanceDate(ticket.getPerformanceDate())
                .festival(ticket.getFestival())
                .festivalFacility(ticket.getFestival().getFcltynm())
                .ticketPrice(ticket.getFestival().getTicketPrice())
                .selectedTicketCount(ticket.getSelectedTicketCount())
                .deliveryMethod(ticket.getDeliveryMethod())
                .build();
    }
}

package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter @Setter
@Builder
public class BookingDetailResponseDTO {

    private String festivalName;
    private int ticketPrice;
    private String posterFile;
    private LocalDateTime performanceDate;
    private int ticketCount;
    private Long sellerId;

    public static BookingDetailResponseDTO fromEntities(Festival festival, Ticket ticket) {
        return BookingDetailResponseDTO.builder()
                .festivalName(festival.getFname())
                .posterFile(festival.getPosterFile())
                .sellerId(festival.getOrganizer())
                .ticketPrice(festival.getTicketPrice())
                .performanceDate(ticket.getPerformanceDate())
                .ticketCount(ticket.getSelectedTicketCount())
                .build();
    }
}
package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// 지인 간 결제 시 반환할 내용

@Data
@Builder
public class TransferOthersResponseDTO {
    private Long receiverId; // 양수자 id

    private Long senderId; // 양도자 id

    private String reservationNumber;
    private int selectedTicketCount; // 티켓 개수
    private LocalDateTime performanceDate;

    private int ticketPrice;
    private String fname;
    private String posterFile;

    public static TransferOthersResponseDTO from(Transfer transfer, Ticket ticket, Festival festival, Long userId) {
        return TransferOthersResponseDTO.builder()
                .reservationNumber(ticket.getReservationNumber())
                .senderId(transfer.getSenderId())
                .receiverId(userId)
                .selectedTicketCount(ticket.getSelectedTicketCount())
                .ticketPrice(festival.getTicketPrice())
                .fname(festival.getFname())
                .posterFile(festival.getPosterFile())
                .performanceDate(ticket.getPerformanceDate())
                .build();
    }
}

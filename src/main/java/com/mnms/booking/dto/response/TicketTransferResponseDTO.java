package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import com.mnms.booking.enums.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketTransferResponseDTO {

    ///  TRANSFER
    private String senderName;
    private TransferType type;
    private LocalDateTime createdAt;
    private String status;

    ///  FESTIVAL
    private String fname;
    private String posterFile;
    private String fcltynm;
    private int ticketPrice;

    ///  TICKET
    private LocalDateTime performanceDate;
    private int selectedTicketCount;

    public static TicketTransferResponseDTO from(Transfer transfer, Ticket ticket, Festival festival) {
        return TicketTransferResponseDTO.builder()
                .senderName(transfer.getSenderName())
                .type(transfer.getType())
                .createdAt(transfer.getCreatedAt())
                .status(String.valueOf(transfer.getStatus()))
                .fname(festival.getFname())
                .posterFile(festival.getPosterFile())
                .fcltynm(festival.getFcltynm())
                .ticketPrice(festival.getTicketPrice())
                .performanceDate(ticket.getPerformanceDate())
                .selectedTicketCount(ticket.getSelectedTicketCount())
                .build();
    }
}
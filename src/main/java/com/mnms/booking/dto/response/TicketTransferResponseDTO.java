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
    private Long transferId;
    private Long senderId;
    private String senderName;
    private TransferType transferType;
    private LocalDateTime createdAt;
    private String status;

    ///  FESTIVAL
    private String fname;
    private String posterFile;
    private String fcltynm;
    private int ticketPrice;
    private int ticketPick;

    ///  TICKET
    private LocalDateTime performanceDate;
    private String reservationNumber;
    private int selectedTicketCount;

    public static TicketTransferResponseDTO from(Transfer transfer, Ticket ticket, Festival festival) {
        return TicketTransferResponseDTO.builder()
                .transferId(transfer.getId())
                .senderId(transfer.getSenderId())
                .senderName(transfer.getSenderName())
                .transferType(transfer.getTransferType())
                .createdAt(transfer.getCreatedAt())
                .status(String.valueOf(transfer.getStatus()))
                .fname(festival.getFname())
                .ticketPick(festival.getTicketPick())
                .posterFile(festival.getPosterFile())
                .fcltynm(festival.getFcltynm())
                .ticketPrice(festival.getTicketPrice())
                .performanceDate(ticket.getPerformanceDate())
                .reservationNumber(ticket.getReservationNumber())
                .selectedTicketCount(ticket.getSelectedTicketCount())
                .build();
    }
}
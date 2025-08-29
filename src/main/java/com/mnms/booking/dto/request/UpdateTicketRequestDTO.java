package com.mnms.booking.dto.request;

import com.mnms.booking.dto.response.BookingResponseDTO;
import com.mnms.booking.dto.response.QrResponseDTO;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequestDTO {
    private Long transferId;
    private Long senderId;
    private TransferStatus transferStatus;

    private TicketType ticketType;
    private String address;

//    public TransferStatus getTransferStatusEnum() {
//        return TransferStatus.valueOf(this.transferStatus);
//    }

}
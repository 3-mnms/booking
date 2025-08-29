package com.mnms.booking.dto.request;

import com.mnms.booking.enums.TicketType;
import com.mnms.booking.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequestDTO {
    private Long transferId;
    private Long senderId;
    private String transferStatus;

    private String deliveryMethod;
    private String address;

    public TransferStatus getTransferStatusEnum() {
        return TransferStatus.valueOf(this.transferStatus);
    }
}
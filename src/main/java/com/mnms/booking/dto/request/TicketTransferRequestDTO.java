package com.mnms.booking.dto.request;

import lombok.Data;

@Data
public class TicketTransferRequestDTO {
    private String reservationNumber;
    private Long recipientId; // 양도받을 가족/지인 ID
    private String transferType; // "FAMILY" 또는 "OTHERS"
    private String senderName;
}
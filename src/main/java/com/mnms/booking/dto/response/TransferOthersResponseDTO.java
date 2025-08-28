package com.mnms.booking.dto.response;

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
}

package com.mnms.booking.dto.response;

import com.mnms.booking.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HostResponseDTO {
    private String reservationNumber;
    private LocalDateTime performanceDate;
    private Long userId;
    private int selectedTicketCount;
    private TicketType deliveryMethod;
    private String address;

    // user info
    private String userName;
    private String phoneNumber;
}

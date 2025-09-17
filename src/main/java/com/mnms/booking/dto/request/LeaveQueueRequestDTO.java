package com.mnms.booking.dto.request;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
public class LeaveQueueRequestDTO {
    private String festivalId;
    private LocalDateTime reservationDate;
    private String userId;
}

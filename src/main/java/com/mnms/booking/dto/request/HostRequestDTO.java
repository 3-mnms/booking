package com.mnms.booking.dto.request;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class HostRequestDTO {
    private String festivalId;
    private LocalDateTime performanceDate;
}

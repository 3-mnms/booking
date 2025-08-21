package com.mnms.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingUserInfoResponseDTO {
    private Long userId;
    private String name;
    private String phone;
    private String address;
}

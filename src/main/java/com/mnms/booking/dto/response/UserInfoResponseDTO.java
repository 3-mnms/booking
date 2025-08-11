package com.mnms.booking.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponseDTO {
    private String phone;
    private String email;
    private String address;
    private String birth;
}

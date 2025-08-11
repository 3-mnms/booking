package com.mnms.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfoResponseDTO {
    private String phone;
    private String email;
    private List<AddressResponseDTO> address;
    private String birth;
}

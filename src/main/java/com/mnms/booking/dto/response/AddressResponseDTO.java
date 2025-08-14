package com.mnms.booking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 주소 응답 DTO", name = "AddressDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {
    @Schema(description = "사용자 주소")
    private String address;

    @Schema(description = "사용자 주소 우편번호")
    private String zipCode;

    @Schema(description = "사용자 주소 기본 배송지 여부")
    private boolean isDefault;
}
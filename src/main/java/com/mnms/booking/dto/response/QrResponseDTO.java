package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QrResponseDTO {

    private Long id;
    private String qrCodeId;
    private String userId;
    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;
    private Boolean used;
    private LocalDateTime usedAt;

    public static QrResponseDTO fromEntity(QrCode qrCode) {
        return QrResponseDTO.builder()
                .id(qrCode.getId())
                .qrCodeId(qrCode.getQrCodeId())
                .userId(qrCode.getUserId())
                .issuedAt(qrCode.getIssuedAt())
                .expiredAt(qrCode.getExpiredAt())
                .used(qrCode.getUsed())
                .usedAt(qrCode.getUsedAt())
                .build();
    }

    public QrCode toEntity() {
        return QrCode.builder()
                .id(this.id)
                .qrCodeId(this.qrCodeId)
                .userId(this.userId)
                .issuedAt(this.issuedAt)
                .expiredAt(this.expiredAt)
                .used(this.used != null ? this.used : false) // 기본값 처리
                .usedAt(this.usedAt)
                .build();
    }

    public static QrResponseDTO create(String userId, String qrCodeId,Festival festival) {
        QrResponseDTO dto = new QrResponseDTO();
        dto.setQrCodeId(qrCodeId);
        dto.setUserId(userId);
        dto.setIssuedAt(LocalDateTime.now());
        dto.setExpiredAt(festival.getPrfpdto().minusMinutes(30));
        return dto;
    }
}

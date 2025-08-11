package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QrResponseDTO {

    private Long id;
    private String qrCodeId;
    private Long userId;
    private LocalDate issuedAt;
    private LocalDate expiredAt;
    private Boolean used;
    private LocalDateTime usedAt;
    private Ticket ticket;

    public static QrResponseDTO fromEntity(QrCode qrCode) {
        return QrResponseDTO.builder()
                .id(qrCode.getId())
                .qrCodeId(qrCode.getQrCodeId())
                .issuedAt(qrCode.getIssuedAt())
                .expiredAt(qrCode.getExpiredAt())
                .used(qrCode.getUsed())
                .usedAt(qrCode.getUsedAt())
                .userId(qrCode.getUserId())
                .build();
    }

    public QrCode toEntity() {
        return QrCode.builder()
                .id(this.id)
                .qrCodeId(this.qrCodeId)
                .issuedAt(this.issuedAt)
                .expiredAt(this.expiredAt)
                .used(this.used != null ? this.used : false) // 기본값 처리
                .usedAt(this.usedAt)
                .userId(this.userId)
                .ticket(this.ticket)
                .build();
    }

    public static QrResponseDTO create(Long userId, String qrCodeId,Festival festival, Ticket ticket) {
        QrResponseDTO dto = new QrResponseDTO();
        dto.setUserId(userId);
        dto.setQrCodeId(qrCodeId);
        dto.setIssuedAt(LocalDate.now());
        dto.setExpiredAt(festival.getFdto());
        dto.setTicket(ticket);
        return dto;
    }
}

package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "qr_code")
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // qr_id

    @Setter
    @Column(name = "qr_code_id", nullable = false)
    private String qrCodeId;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "used", nullable = false)
    private Boolean used;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket; // ticket_id

    // 비지니스 로직
    public void markAsUsed() {
        if (this.used) {
            throw new IllegalStateException("이미 사용된 QR 코드입니다.");
        }
        if (isExpired()) {
            throw new IllegalStateException("QR 코드의 유효기간이 지났습니다.");
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    private boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}
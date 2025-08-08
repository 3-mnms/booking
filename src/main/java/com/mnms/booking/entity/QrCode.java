package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@Table(name = "qr_code")
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // qr_id

    @Column(name = "qr_code_id", nullable = false)
    private String qrCodeId;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "used", nullable = false)
    private Boolean used;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    private Ticket ticket; // ticket_id
}
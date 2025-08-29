package com.mnms.booking.entity;

import com.mnms.booking.enums.TransferStatus;
import com.mnms.booking.enums.TransferType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    private String senderName;

    private Long receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    private TransferType type;

    @Setter
    @Builder.Default
    private TransferStatus status = TransferStatus.REQUESTED;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}

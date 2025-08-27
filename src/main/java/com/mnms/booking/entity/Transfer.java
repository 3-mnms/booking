package com.mnms.booking.entity;

import com.mnms.booking.enums.TransferStatus;
import com.mnms.booking.enums.TransferType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    private String senderName;

    private Long receiverId;

    @Enumerated(EnumType.STRING)
    private TransferType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransferStatus status = TransferStatus.REQUESTED;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}

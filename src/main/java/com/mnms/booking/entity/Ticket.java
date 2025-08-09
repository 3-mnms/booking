package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ticket_id

    @Column(name = "reservation_number")
    private String reservationNumber; // 예매번호

    @Column(name = "reservation_status")
    private Boolean reservationStatus; // 예매 상태

    @Column(name = "delivery_method")
    private TicketType deliveryMethod; // 수령방법

    @Column(name = "user_id")
    private Long userId; // 예매자 id

    @Column(name = "reservation_date")
    private LocalDate reservationDate; // 예매 날짜

    @Column(name = "delivery_date")
    private LocalDate deliveryDate; // 택배 날짜

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QrCode qrCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;
}
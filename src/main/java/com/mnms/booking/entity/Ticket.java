package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Builder @Getter
@Table(name = "ticket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ticket_id

    @Column(name = "reservation_number")
    private String reservationNumber; // 예매번호

    @Column(name = "reservation_status")
    private ReservationStatus reservationStatus; // 예매상태

    @Column(name = "delivery_method")
    private TicketType deliveryMethod; // 수령방법

    @Column(name = "user_id")
    private Long userId; // 예매자 id

    @Column(name = "reservation_date")
    private LocalDate reservationDate; // 예매를 수행한 날짜

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate; // 택배 날짜

    @Column(name = "performance_date")
    private LocalDateTime performanceDate; // 선택한 공연 날짜 시간

    @Column(name = "selected_ticket_count")
    private int selectedTicketCount; // 선택 매수

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QrCode qrCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    // setter
    public void setQrCode(QrCode qrCode){
        this.qrCode= qrCode;
    }
    public void setDeliveryMethod(TicketType deliveryMethod){this.deliveryMethod = deliveryMethod;}
    public void setDeliveryDate(LocalDateTime deliveryDate){ this.deliveryDate = deliveryDate;}
}
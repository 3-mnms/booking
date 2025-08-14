package com.mnms.booking.entity;

import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TicketType;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder @Getter
@Table(name = "ticket")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ticket_id

    @Column(name = "reservation_number")
    private String reservationNumber; // 예매번호

    @Setter
    @Column(name = "reservation_status")
    private ReservationStatus reservationStatus; // 예매상태

    @Setter
    @Column(name = "delivery_method")
    private TicketType deliveryMethod; // 수령방법

    @Column(name = "user_id")
    private Long userId; // 예매자 id

    @Column(name = "reservation_date")
    private LocalDate reservationDate; // 예매를 수행한 날짜

    @Setter
    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate; // 택배 날짜

    @Column(name = "performance_date")
    private LocalDateTime performanceDate; // 선택한 공연 날짜 시간

    @Column(name = "selected_ticket_count")
    private int selectedTicketCount; // 선택 매수

    @Setter
    @Builder.Default
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QrCode> qrCodes = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

}
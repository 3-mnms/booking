package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // DB PK (자동증가)

    @Column(name = "event_type")
    private EventType eventType;

    @Column(name = "festival_id", unique = true, nullable = false, length = 20)
    private String festivalId; // 공연 고유 ID (PF000001)

    @Column(name = "fname", nullable = false)
    private String fname; // 공연명

    @Column(name = "fdfrom", nullable = false)
    private LocalDate fdfrom; // 공연 시작일 (YYYY-MM-DD)

    @Column(name = "fdto", nullable = false)
    private LocalDate fdto; // 공연 종료일 (YYYY-MM-DD)

    @Column(name = "poster_file")
    private String posterFile; // 공연 대표 이미지 URL

    @Column(name = "fcltynm")
    private String fcltynm; // 공연장 장소

    @Column(name = "ticket_pick")
    private int ticketPick; // 티켓 배송 방법 (1=배송만, 2=qr만, 3=둘다)

    @Column(name = "max_purchase")
    private int maxPurchase; // 1회 최대 구매 가능 수량 (1~4)

    @Column(name = "ticket_price")
    private int ticketPrice; // 티켓 가격 (원 단위)

    @Column(name = "available_nop")
    private int availableNOP; // 수용인원

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();
}
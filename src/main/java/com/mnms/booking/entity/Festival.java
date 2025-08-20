package com.mnms.booking.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mnms.booking.kafka.dto.FestivalEventDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // DB PK (자동증가)

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

    @Column(name = "organizer")
    private Long organizer;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    @Setter
    private List<Schedule> schedules = new ArrayList<>();

    public static Festival fromDto(FestivalEventDTO dto) {
        return Festival.builder()
                .festivalId(dto.getId())
                .fname(dto.getFname())
                .fdfrom(dto.getFdfrom())
                .fdto(dto.getFdto())
                .posterFile(dto.getPosterFile())
                .fcltynm(dto.getFcltynm())
                .ticketPick(dto.getTicketPick())
                .maxPurchase(dto.getMaxPurchase())
                .ticketPrice(dto.getTicketPrice())
                .availableNOP(dto.getAvailableNOP())
                .organizer(dto.getUserId())
                .build();
    }

    public void updateFromDto(FestivalEventDTO dto) {
        if (dto.getFname() != null) this.fname = dto.getFname();
        if (dto.getUserId() != null) this.organizer = dto.getUserId();
        if (dto.getPosterFile() != null) this.posterFile = dto.getPosterFile();
        if (dto.getFdfrom() != null) this.fdfrom = dto.getFdfrom();
        if (dto.getFdto() != null) this.fdto = dto.getFdto();
        if (dto.getFcltynm() != null) this.fcltynm = dto.getFcltynm();
        if (dto.getMaxPurchase() != 0) this.maxPurchase = dto.getMaxPurchase();
        if (dto.getAvailableNOP() != 0) this.availableNOP = dto.getAvailableNOP();
        if (dto.getTicketPrice() != 0) this.ticketPrice = dto.getTicketPrice();
        if (dto.getTicketPick() != 0) this.ticketPick = dto.getTicketPick();
    }

    @Transactional
    public void mergeSchedules(List<Schedule> updatedSchedules) {
        Map<Long, Schedule> existingMap = this.schedules.stream()
                .collect(Collectors.toMap(Schedule::getScheduleId, s -> s));

        List<Schedule> merged = new ArrayList<>();

        for (Schedule updated : updatedSchedules) {
            Schedule schedule = existingMap.get(updated.getScheduleId());
            if (schedule != null) {
                schedule.setScheduleId(updated.getScheduleId());
                schedule.setDayOfWeek(updated.getDayOfWeek());
                schedule.setTime(updated.getTime());
                merged.add(schedule);
            } else {
                updated.setFestival(this);
                merged.add(updated);
            }
        }

        // 기존에 있고 이번 업데이트에 없는 스케줄은 제거
        this.schedules.clear();
        this.schedules.addAll(merged);
    }
}
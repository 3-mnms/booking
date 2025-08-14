package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FestivalDetailResponseDTO {
    /// festival 정보
    private String fname; // festival 이름
    private int ticketPrice;
    private String posterFile;
    private int maxPurchase; // 최대 구매 가능 매수
    private List<ScheduleResponseDTO> schedules; // 공연 스케줄 목록

    ///  ticket 정보
    private LocalDateTime performanceDate; // 선택 날짜,시간

    public static FestivalDetailResponseDTO fromEntity(Festival festival, LocalDateTime performanceDate, List<ScheduleResponseDTO> schedules) {
        return FestivalDetailResponseDTO.builder()
                .fname(festival.getFname())
                .ticketPrice(festival.getTicketPrice())
                .posterFile(festival.getPosterFile())
                .maxPurchase(festival.getMaxPurchase())
                .performanceDate(performanceDate)
                .schedules(schedules)
                .build();
    }

}

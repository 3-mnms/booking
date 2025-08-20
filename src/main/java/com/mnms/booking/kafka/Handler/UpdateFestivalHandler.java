package com.mnms.booking.kafka.Handler;

import com.mnms.booking.entity.Schedule;
import com.mnms.booking.kafka.dto.FestivalEventDTO;
import com.mnms.booking.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateFestivalHandler implements FestivalEventHandler {

    private final FestivalRepository festivalRepository;

    @Override
    @Transactional
    public void handle(FestivalEventDTO dto) {
        festivalRepository.findByFestivalId(dto.getId())
                .ifPresent(festival -> {
                    festival.updateFromDto(dto);

                    List<Schedule> updatedSchedules = dto.getSchedules() == null ? List.of() :
                            dto.getSchedules().stream()
                                    .map(s -> Schedule.fromDto(s, festival))
                                    .toList();

                    festival.mergeSchedules(updatedSchedules);
                    festivalRepository.save(festival);
                });
    }
}

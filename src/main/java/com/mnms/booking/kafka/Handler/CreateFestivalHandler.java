package com.mnms.booking.kafka.Handler;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Schedule;
import com.mnms.booking.kafka.dto.FestivalEventDTO;
import com.mnms.booking.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateFestivalHandler implements FestivalEventHandler {

    private final FestivalRepository festivalRepository;

    @Override
    public void handle(FestivalEventDTO dto) {
        Festival festival = Festival.fromDto(dto);

        List<Schedule> schedules = dto.getSchedules().stream()
                .map(s -> Schedule.fromDto(s, festival))
                .toList();

        festival.setSchedules(schedules);
        festivalRepository.save(festival);
    }
}
package com.mnms.kafka.booking.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Schedule;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.kafka.booking.dto.FestivalEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FestivalListener {

    private final FestivalRepository festivalRepository;

    @KafkaListener(topics = "${app.kafka.topic.festival-event}", groupId = "festival-service-group")
    public void consumeFestival(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        FestivalEventDTO dto = objectMapper.readValue(message, FestivalEventDTO.class);

        Festival festival = Festival.fromDto(dto);

        List<Schedule> schedules = dto.getSchedules().stream()
                .map(s -> Schedule.fromDto(s, festival))
                .toList();

        festival.setSchedules(schedules);
        festivalRepository.save(festival);
    }
}
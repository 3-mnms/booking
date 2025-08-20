package com.mnms.booking.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mnms.booking.kafka.Handler.CreateFestivalHandler;
import com.mnms.booking.kafka.Handler.DeleteFestivalHandler;
import com.mnms.booking.kafka.Handler.FestivalEventHandler;
import com.mnms.booking.kafka.Handler.UpdateFestivalHandler;
import com.mnms.booking.kafka.dto.FestivalEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalListener {

    private final CreateFestivalHandler createHandler;
    private final UpdateFestivalHandler updateHandler;
    private final DeleteFestivalHandler deleteHandler;

    @KafkaListener(topics = "${app.kafka.topic.festival-event}", groupId = "festival-service-group")
    public void consumeFestival(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        FestivalEventDTO dto = objectMapper.readValue(message, FestivalEventDTO.class);

        String eventType = dto.getEventType();
        FestivalEventHandler handler = switch (eventType.toUpperCase()) {
            case "FESTIVAL_CREATED" -> createHandler;
            case "FESTIVAL_UPDATED" -> updateHandler;
            case "FESTIVAL_DELETED" -> deleteHandler;
            default -> null;
        };

        if (handler != null) {
            handler.handle(dto);
        } else {
            System.out.println("Unknown eventType: " + eventType);
        }
    }
}
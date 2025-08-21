package com.mnms.booking.kafka.Handler;

import com.mnms.booking.kafka.dto.FestivalEventDTO;
import com.mnms.booking.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteFestivalHandler implements FestivalEventHandler {

    private final FestivalRepository festivalRepository;

    @Override
    public void handle(FestivalEventDTO dto) {
        festivalRepository.findByFestivalId(dto.getId()).ifPresent(festivalRepository::delete);
    }
}
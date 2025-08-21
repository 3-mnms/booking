package com.mnms.booking.kafka.Handler;

import com.mnms.booking.kafka.dto.FestivalEventDTO;

public interface FestivalEventHandler {
    void handle(FestivalEventDTO dto);
}
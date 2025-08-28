package com.mnms.booking.service;

import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsQueryService {

    private final TicketRepository ticketRepository;

    public List<LocalDateTime> getPerformanceDatesByFestivalId(String festivalId) {
        List<LocalDateTime> performanceDates = ticketRepository.findDistinctPerformanceDateByFestivalId(festivalId);
        if (performanceDates.isEmpty()) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND);
        }
        return performanceDates;
    }
}
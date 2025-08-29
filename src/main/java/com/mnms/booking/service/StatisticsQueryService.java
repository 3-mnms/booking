package com.mnms.booking.service;

import com.mnms.booking.dto.response.StatisticsBookingDTO;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsQueryService {

    private final TicketRepository ticketRepository;
    private final FestivalRepository festivalRepository;

    public void validateHostOrAdminAccess(String festivalId, String userId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        Long longUserId;
        try {
            longUserId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.USER_INVALID);
        }
        if (!festivalRepository.existsByFestivalIdAndOrganizer(festivalId, longUserId)) {
            throw new BusinessException(ErrorCode.STATISTICS_ACCESS_DENIED);
        }
    }

    public List<LocalDateTime> getPerformanceDatesByFestivalId(String festivalId) {
        List<LocalDateTime> performanceDates = ticketRepository.findDistinctPerformanceDateByFestivalId(festivalId);
        if (performanceDates.isEmpty()) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND);
        }
        return performanceDates;
    }

    public List<StatisticsBookingDTO> getBookingSummary(String festivalId) {
        int availableCapacity = festivalRepository.findByFestivalId(festivalId)
                .map(festival -> festival.getAvailableNOP())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 유효한 티켓을 가져오는 올바른 방법
        List<StatisticsBookingDTO> bookingSummary = ticketRepository.findBookedSummary(festivalId, ReservationStatus.CONFIRMED);

        if (bookingSummary.isEmpty()) {
            return new ArrayList<>();
        }

        bookingSummary.forEach(dto -> dto.setAvailableNOP(availableCapacity));

        return bookingSummary;
    }
}
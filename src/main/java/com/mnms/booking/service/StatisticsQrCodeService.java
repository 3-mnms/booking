package com.mnms.booking.service;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsQrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;
    private final TicketRepository ticketRepository;

    public Map<String, Object> getPerformanceAttendanceStatistics(String festivalId, LocalDateTime performanceDate, String userId, boolean isHost) {

        Long longUserId = Long.valueOf(userId);

        // 1. 접근 권한 검증
        if (isHost) {
            boolean isOwner = festivalRepository.existsByFestivalIdAndOrganizer(festivalId, longUserId);
            if (!isOwner) {
                throw new AccessDeniedException("이 공연의 통계 정보에 접근할 권한이 없습니다.");
            }
        } else {
            boolean hasTicket = ticketRepository.existsByUserIdAndFestivalId(longUserId, festivalId);
            if (!hasTicket) {
                throw new AccessDeniedException("이 공연의 통계 정보에 접근할 권한이 없습니다.");
            }
        }

        // 2. 통계 로직
        Optional<Festival> festivalOpt = festivalRepository.findByFestivalId(festivalId);
        int capacity = festivalOpt.map(Festival::getAvailableNOP).orElse(0);

        int admittedCount = qrCodeRepository.countAdmittedAttendees(festivalId, performanceDate);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("festivalId", festivalId);
        statistics.put("performanceDate", performanceDate);
        statistics.put("capacity", capacity);
        statistics.put("admittedCount", admittedCount);

        return statistics;
    }
}
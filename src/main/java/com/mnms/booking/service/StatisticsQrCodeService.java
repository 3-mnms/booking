package com.mnms.booking.service;

import com.mnms.booking.dto.response.StatisticsQrCodeResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticsQrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final FestivalRepository festivalRepository;
    private final TicketRepository ticketRepository;

    public StatisticsQrCodeResponseDTO getPerformanceEnterStatistics(String festivalId, LocalDateTime performanceDate, String userId, boolean isHost, boolean isAdmin) {

        // 1. ADMIN 권한 확인: ADMIN은 모든 검증을 건너뛰고 바로 통계 조회
        if (!isAdmin) {
            Long longUserId;
            try {
                // userId가 유효한 Long 값인지 확인
                longUserId = Long.valueOf(userId);
            } catch (NumberFormatException e) {
                // 숫자로 변환할 수 없는 경우 예외 처리
                throw new BusinessException(ErrorCode.USER_INVALID);
            }

            // 2. ADMIN이 아니면 기존 권한 검증 로직 실행
            if (isHost) {
                Festival festival = festivalRepository.findByFestivalIdAndOrganizer(festivalId, longUserId);
                if (festival == null) {
                    throw new BusinessException(ErrorCode.STATISTICS_ACCESS_DENIED);
                }
            } else {
                boolean hasTicket = ticketRepository.existsByUserIdAndFestivalId(longUserId, festivalId);
                if (!hasTicket) {
                    throw new BusinessException(ErrorCode.STATISTICS_ACCESS_DENIED);
                }
            }
        }

        // 3. 통계 로직
        int availableNOP = festivalRepository.findByFestivalId(festivalId)
                .map(Festival::getAvailableNOP)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        int checkedInCount = qrCodeRepository.countAdmittedAttendees(festivalId, performanceDate);

        return new StatisticsQrCodeResponseDTO(festivalId, performanceDate, availableNOP, checkedInCount);
    }
}
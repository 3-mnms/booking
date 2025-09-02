package com.mnms.booking.service;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;

    ///특정 festivalId에 해당하는 공연의 수용 인원을 조회
    public int getCapacity(String festivalId) {
        Festival festival = festivalRepository.findByFestivalId(festivalId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return festival.getAvailableNOP();
    }
}
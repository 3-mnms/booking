package com.mnms.booking.repository;

import com.mnms.booking.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    // 필요한 커스텀 쿼리가 있으면 여기에 추가 작성 가능
}
package com.mnms.booking.repository;

import com.mnms.booking.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    // 필요한 커스텀 쿼리가 있으면 여기에 추가 작성 가능
    Optional<QrCode> findByQrCodeId(String qrCodeId);
}



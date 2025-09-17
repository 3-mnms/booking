package com.mnms.booking.repository;

import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    // 필요한 커스텀 쿼리가 있으면 여기에 추가 작성 가능
    Optional<QrCode> findByQrCodeId(String qrCodeId);
    Boolean existsByQrCodeId(String qrCodeId);
    Optional<List<QrCode>> findByTicketId(Long ticketId);

    // 특정 페스티벌의 특정 공연 날짜에 입장(used=true)한 인원 수를 집계
    @Query("SELECT COUNT(q) FROM QrCode q JOIN q.ticket t WHERE q.used = true AND t.festival.festivalId = :festivalId AND t.performanceDate = :performanceDate")
    int countAdmittedAttendees(@Param("festivalId") String festivalId, @Param("performanceDate") LocalDateTime performanceDate);;

    boolean existsByTicketIdAndUsedTrue(Long ticketId);
    boolean existsByTicket_IdAndUsedTrue(Long ticketId);
}



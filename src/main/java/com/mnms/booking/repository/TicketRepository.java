package com.mnms.booking.repository;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    int countByUserIdAndFestivalId(Long userId, Long festivalId);

    @Query("SELECT t FROM Ticket t WHERE t.festival.festivalId = :festivalId AND t.userId = :userId AND t.performanceDate = :performanceDate")
    Optional<Ticket> findByFestivalIdAndUserIdAndPerformanceDate(
            @Param("festivalId") String festivalId,
            @Param("userId") Long userId,
            @Param("performanceDate") LocalDateTime performanceDate
    );
}

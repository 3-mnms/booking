package com.mnms.booking.repository;

import com.mnms.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    int countByUserIdAndFestivalIdAndPerformanceDate(Long userId, Long festivalId, LocalDateTime performanceDate);

    @Query("SELECT COALESCE(SUM(t.selectedTicketCount), 0) " +
            "FROM Ticket t " +
            "WHERE t.userId = :userId " +
            "AND t.festival.festivalId = :festivalId " +
            "AND t.performanceDate >= :startDate " +
            "AND t.performanceDate < :endDate")
    Long sumSelectedTicketCount(
            @Param("userId") Long userId,
            @Param("festivalId") String festivalId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query("SELECT t FROM Ticket t WHERE t.festival.festivalId = :festivalId AND t.userId = :userId AND t.reservationNumber = :reservationNumber")
    Optional<Ticket> findByFestivalIdAndUserIdAndReservationNumber(
            @Param("festivalId") String festivalId,
            @Param("userId") Long userId,
            @Param("reservationNumber") String reservationNumber
    );

    Optional<Ticket> findByReservationNumber(String reservationNumber);
}

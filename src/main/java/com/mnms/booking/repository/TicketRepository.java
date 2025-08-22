package com.mnms.booking.repository;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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


    @Query("SELECT t " +
            "FROM Ticket t " +
            "WHERE t.festival.festivalId = :festivalId AND t.userId = :userId AND t.reservationNumber = :reservationNumber")
    Optional<Ticket> findByFestivalIdAndUserIdAndReservationNumber(
            @Param("festivalId") String festivalId,
            @Param("userId") Long userId,
            @Param("reservationNumber") String reservationNumber
    );

    Optional<Ticket> findByReservationNumber(String reservationNumber);

    // host
    @Query("SELECT DISTINCT t.userId " +
            "FROM Ticket t " +
            "WHERE t.festival.festivalId = :festivalId " +
            "AND t.performanceDate = :performanceDate")
    List<Long> findDistinctUserIdsByFestivalIdAndPerformanceDate(@Param("festivalId") String festivalId,
                                                           @Param("performanceDate") LocalDateTime performanceDate);


    @Query("SELECT t " +
            "FROM Ticket t " +
            "WHERE t.festival.festivalId = :festivalId ")
    List<Ticket> findByFestivalId(String festivalId);

    @Query("SELECT t.selectedTicketCount " +
            "FROM Ticket t " +
            "WHERE t.reservationNumber = :reservationNumber")
    Long findSelectedTicketCountByReservationNumber(@Param("reservationNumber") String reservationNumber);

    @Query("SELECT COALESCE(SUM(t.selectedTicketCount), 0) " +
            "FROM Ticket t " +
            "WHERE t.festival.festivalId = :festivalId " +
            "AND t.performanceDate = :performanceDate " +
            "AND t.reservationStatus IN (:statuses)")
    int getTotalSelectedTicketCount(@Param("festivalId") String festivalId,
                                    @Param("performanceDate") LocalDateTime performanceDate,
                                    @Param("statuses") List<ReservationStatus> statuses);
}

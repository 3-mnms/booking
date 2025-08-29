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

    List<Ticket> findByUserIdAndReservationStatusIn(Long userId, List<ReservationStatus> statuses);
    Optional<Ticket> findByUserIdAndReservationNumber(Long userId, String reservationNumber);

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.festival.festivalId = :festivalId " +
            "AND t.reservationStatus = :status")
    List<Ticket> findByFestivalIdAndReservationStatus(@Param("festivalId") String festivalId,
                                                      @Param("status") ReservationStatus status);

    Optional<List<Ticket>> findByUserId(Long userId);

    // userId와 festivalId가 일치하는 티켓이 존재하는지 확인
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Ticket t WHERE t.userId = :userId AND t.festival.festivalId = :festivalId")
    boolean existsByUserIdAndFestivalId(@Param("userId") Long userId, @Param("festivalId") String festivalId);

    // 특정 festivalId에 대한 유효한 공연 날짜-시간을 모두 조회
    @Query("SELECT DISTINCT t.performanceDate FROM Ticket t WHERE t.festival.festivalId = :festivalId")
    List<LocalDateTime> findDistinctPerformanceDateByFestivalId(@Param("festivalId") String festivalId);
}

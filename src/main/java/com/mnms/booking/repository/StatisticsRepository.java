package com.mnms.booking.repository;

import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface StatisticsRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t.userId FROM Ticket t WHERE t.festival.festivalId = :festivalId")
    List<String> findUserIdsByFestivalId(String festivalId);

    @Query("SELECT DISTINCT CAST(t.userId AS string) FROM Ticket t " +
            "WHERE t.festival.festivalId = :festivalId AND t.reservationStatus = :status")
    List<String> findUserIdsByFestivalIdAndReservationStatus(@Param("festivalId") String festivalId,
                                                             @Param("status") ReservationStatus status);
}
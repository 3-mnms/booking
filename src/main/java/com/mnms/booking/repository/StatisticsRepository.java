package com.mnms.booking.repository;

import com.mnms.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface StatisticsRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t.userId FROM Ticket t WHERE t.festival.festivalId = :festivalId")
    List<String> findUserIdsByFestivalId(String festivalId);
}
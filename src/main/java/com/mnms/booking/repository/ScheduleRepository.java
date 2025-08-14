package com.mnms.booking.repository;

import com.mnms.booking.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository  extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.festival.festivalId = :festivalId")
    List<Schedule> findByFestivalId(@Param("festivalId") String festivalId);
}

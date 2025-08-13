package com.mnms.booking.repository;

import com.mnms.booking.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository  extends JpaRepository<Schedule, Long> {
    List<Schedule> findByFestivalId(String festivalId);
}

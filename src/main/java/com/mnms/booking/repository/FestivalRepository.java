package com.mnms.booking.repository;

import com.mnms.booking.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Optional<Festival> findByFestivalId(String festivalId);
    List<Festival> findByFestivalIdAndOrganizer(String festivalId , Long organizer);

    // festivalId와 organizer(userId)가 일치하는 엔티티가 존재하는지 확인
    boolean existsByFestivalIdAndOrganizer(String festivalId, Long organizer);
}
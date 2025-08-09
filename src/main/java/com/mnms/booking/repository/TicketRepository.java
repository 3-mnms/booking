package com.mnms.booking.repository;

import com.mnms.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    int countByUserIdAndFestivalId(Long userId, Long festivalId);
}

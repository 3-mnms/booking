package com.mnms.booking.repository;

import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Transfer findByTicket(Ticket ticket);
    List<Transfer> findByReceiverId(Long userId);
    Optional<Transfer> findById(Long id);
}
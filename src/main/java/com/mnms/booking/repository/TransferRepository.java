package com.mnms.booking.repository;

import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import com.mnms.booking.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Transfer findByTicket(Ticket ticket);
    Optional<Transfer> findById(Long id);
    boolean existsByTicket(Ticket ticket);

    @Query("SELECT t FROM Transfer t " +
            "JOIN FETCH t.ticket ticket " +
            "JOIN FETCH ticket.festival " +
            "WHERE t.receiverId = :receiverId " +
            "AND t.status NOT IN :excludedStatuses")
    List<Transfer> findByReceiverIdWithTicketAndFestival(
            @Param("receiverId") Long receiverId,
            @Param("excludedStatuses") List<TransferStatus> excludedStatuses);
}
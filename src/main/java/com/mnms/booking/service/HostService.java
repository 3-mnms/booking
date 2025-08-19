package com.mnms.booking.service;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class HostService {

    private final TicketRepository ticketRepository;

    public List<Long> getBookingsByOrganizer(HostRequestDTO request) {
        return ticketRepository.findDistinctUserIdsByFestivalIdAndPerformanceDate(request.getFestivalId(), request.getPerformanceDate());
    }
}

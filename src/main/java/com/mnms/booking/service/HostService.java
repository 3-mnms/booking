package com.mnms.booking.service;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.dto.response.BookingUserInfoResponseDTO;
import com.mnms.booking.dto.response.HostResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class HostService {

    private final TicketRepository ticketRepository;
    private final FestivalRepository festivalRepository;
    private final UserApiClient userApiClient;

    public List<Long> getBookingsByOrganizer(HostRequestDTO request) {
        return ticketRepository.findDistinctUserIdsByFestivalIdAndPerformanceDate(request.getFestivalId(), request.getPerformanceDate());
    }

    public List<HostResponseDTO> getBookingInfoByHost(Long hostUserId) {
        List<Festival> festivals = festivalRepository.findByOrganizer(hostUserId);
        if (festivals == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND);
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Festival festival : festivals) {
            tickets.addAll(ticketRepository.findByFestivalId(festival.getFestivalId()));
        }

        List<Long> userIds = tickets.stream()
                .map(Ticket::getUserId)
                .distinct()
                .toList();

        List<BookingUserInfoResponseDTO> users = userApiClient.getUsersByIds(userIds);
        Map<Long, BookingUserInfoResponseDTO> userMap = users.stream()
                .collect(Collectors.toMap(BookingUserInfoResponseDTO::getUserId, u -> u));

        return tickets.stream()
                .map(t -> {
                    BookingUserInfoResponseDTO user = userMap.get(t.getUserId());
                    return new HostResponseDTO(
                            t.getReservationNumber(),
                            t.getPerformanceDate(),
                            t.getUserId(),
                            t.getSelectedTicketCount(),
                            t.getDeliveryMethod(),
                            user != null ? user.getName() : null,
                            user != null ? user.getPhone() : null,
                            user != null ? user.getAddress() : null
                    );
                })
                .toList();
    }
}

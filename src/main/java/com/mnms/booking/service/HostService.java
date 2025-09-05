package com.mnms.booking.service;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.dto.response.BookingUserInfoResponseDTO;
import com.mnms.booking.dto.response.HostResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostService {

    private final TicketRepository ticketRepository;
    private final FestivalRepository festivalRepository;
    private final UserApiClient userApiClient;

    public List<Long> getBookingsByOrganizer(HostRequestDTO request) {
        return ticketRepository
                .findDistinctUserIdsByFestivalIdAndPerformanceDateAndReservationStatus(
                        request.getFestivalId(),
                        request.getPerformanceDate(),
                        ReservationStatus.CONFIRMED
                );
    }

    @Transactional
    public List<HostResponseDTO> getBookingInfoByHost(String festivalId, Long hostUserId, List<String> role) {

        Festival festival;
        if (role.contains("ROLE_ADMIN")) {
            festival = festivalRepository.findByFestivalId(festivalId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        } else {
            festival = festivalRepository.findByFestivalIdAndOrganizer(festivalId, hostUserId);
        }

        if (festival == null) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND);
        }

        List<Ticket> tickets = new ArrayList<>(ticketRepository.findByFestivalIdAndReservationStatus(festival.getFestivalId(), ReservationStatus.CONFIRMED));

        if (tickets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = tickets.stream()
                .map(Ticket::getUserId)
                .distinct()
                .toList();

        List<BookingUserInfoResponseDTO> users;
        try {
            users = userApiClient.getUsersByIds(userIds);
            if (users == null) users = Collections.emptyList();
        } catch (WebClientResponseException e) {
            throw new BusinessException(ErrorCode.USER_API_ERROR);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
        }

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
                            t.getAddress(),
                            user != null ? user.getName() : null,
                            user != null ? user.getPhone() : null
                    );
                })
                .toList();
    }
}

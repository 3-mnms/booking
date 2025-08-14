package com.mnms.booking.service;


import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.BookingDetailResponseDTO;
import com.mnms.booking.dto.response.FestivalDetailResponseDTO;
import com.mnms.booking.dto.response.ScheduleResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.FestivalRepository;
import com.mnms.booking.repository.ScheduleRepository;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingQueryService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final TicketRepository ticketRepository;
    private final FestivalRepository festivalRepository;
    private final ScheduleRepository scheduleRepository;

    /// 1차 : 조회
    public FestivalDetailResponseDTO getFestivalDetail(BookingSelectRequestDTO request) {
        Festival festival = getFestivalOrThrow(request.getFestivalId());
        LocalDateTime performanceDate = request.getPerformanceDate();

        validatePerformanceDate(festival, performanceDate);
        validateScheduleExists(festival, performanceDate);
        List<ScheduleResponseDTO> scheduleDTOs = getSchedules(festival);
        return FestivalDetailResponseDTO.fromEntity(festival, performanceDate, scheduleDTOs);
    }

    ///  2차 : 조회
    public BookingDetailResponseDTO getFestivalBookingDetail(BookingRequestDTO request, Long userId) {
        Festival festival = getFestivalOrThrow(request.getFestivalId());
        Ticket ticket = getTicketByReservationNumberOrThrow(request.getReservationNumber());

        return BookingDetailResponseDTO.fromEntities(festival, ticket);
    }

    ///  기타
    private void validatePerformanceDate(Festival festival, LocalDateTime performanceDate) {
        LocalDate date = performanceDate.toLocalDate();
        if (date.isBefore(festival.getFdfrom()) || date.isAfter(festival.getFdto())) {
            throw new BusinessException(ErrorCode.FESTIVAL_INVALID_DATE);
        }
    }

    private void validateScheduleExists(Festival festival, LocalDateTime performanceDate) {
        String dayOfWeek = performanceDate.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase();

        boolean exists = festival.getSchedules().stream()
                .anyMatch(s -> s.getDayOfWeek().equalsIgnoreCase(dayOfWeek)
                        && LocalTime.parse(s.getTime(), TIME_FORMATTER).equals(performanceDate.toLocalTime()));

        if (!exists) throw new BusinessException(ErrorCode.FESTIVAL_INVALID_TIME);
    }

    private Festival getFestivalOrThrow(String festivalId) {
        return festivalRepository.findByFestivalId(festivalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    private Ticket getTicketByReservationNumberOrThrow(String reservationNumber) {
        return ticketRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private List<ScheduleResponseDTO> getSchedules(Festival festival) {
        return scheduleRepository.findByFestivalId(festival.getFestivalId())
                .stream()
                .map(s -> ScheduleResponseDTO.builder()
                        .scheduleId(s.getScheduleId())
                        .dayOfWeek(s.getDayOfWeek())
                        .time(s.getTime())
                        .build())
                .toList();
    }
}

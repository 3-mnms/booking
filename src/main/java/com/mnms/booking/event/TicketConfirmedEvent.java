package com.mnms.booking.event;

import com.mnms.booking.dto.request.TicketRequestDTO;

public record TicketConfirmedEvent(TicketRequestDTO ticket) {}
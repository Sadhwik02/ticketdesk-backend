package com.ticketdesk.service;

import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SearchService {

    Page<TicketResponse> searchTickets(
            String keyword,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            Long engineerId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);
}

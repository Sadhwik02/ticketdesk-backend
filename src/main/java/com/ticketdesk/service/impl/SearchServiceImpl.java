package com.ticketdesk.service.impl;

import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.entity.Ticket;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.mapper.TicketMapper;
import com.ticketdesk.repository.TicketRepository;
import com.ticketdesk.service.SearchService;
import com.ticketdesk.validation.TicketSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final TicketRepository ticketRepository;
    private final TicketMapper     ticketMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> searchTickets(
            final String keyword,
            final TicketStatus status,
            final TicketPriority priority,
            final TicketCategory category,
            final Long engineerId,
            final LocalDate startDate,
            final LocalDate endDate,
            final Pageable pageable) {

        log.debug("Searching tickets with keyword='{}' status='{}' priority='{}' category='{}'",
                keyword, status, priority, category);

        final Specification<Ticket> spec = TicketSpecification.hasKeyword(keyword)
                .and(TicketSpecification.hasStatus(status))
                .and(TicketSpecification.hasPriority(priority))
                .and(TicketSpecification.hasCategory(category))
                .and(TicketSpecification.assignedToEngineer(engineerId))
                .and(TicketSpecification.createdBetween(startDate, endDate));

        return ticketRepository.findAll(spec, pageable).map(ticketMapper::toResponse);
    }
}

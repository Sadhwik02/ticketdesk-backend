package com.ticketdesk.service.impl;

import com.ticketdesk.dto.response.DashboardResponse;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.repository.TicketRepository;
import com.ticketdesk.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        log.debug("Fetching dashboard statistics");

        // Status counts
        final long totalTickets    = ticketRepository.count();
        final long openTickets     = ticketRepository.countByStatus(TicketStatus.OPEN);
        final long inProgressCount = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        final long resolvedCount   = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        final long closedCount     = ticketRepository.countByStatus(TicketStatus.CLOSED);

        // Priority counts
        final Map<String, Long> byPriority = new LinkedHashMap<>();
        for (final TicketPriority p : TicketPriority.values()) {
            byPriority.put(p.name(), ticketRepository.countByPriority(p));
        }

        // Category counts
        final Map<String, Long> byCategory = new LinkedHashMap<>();
        for (final TicketCategory c : TicketCategory.values()) {
            byCategory.put(c.name(), ticketRepository.countByCategory(c));
        }

        // Engineer counts
        final Map<String, Long> byEngineer = new LinkedHashMap<>();
        final List<Object[]> engineerData  = ticketRepository.countTicketsByEngineer();
        for (final Object[] row : engineerData) {
            final String name  = row[1] + " " + row[2];  // firstName + lastName
            final Long   count = (Long) row[3];
            byEngineer.put(name, count);
        }

        return DashboardResponse.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressCount)
                .resolvedTickets(resolvedCount)
                .closedTickets(closedCount)
                .ticketsByPriority(byPriority)
                .ticketsByCategory(byCategory)
                .ticketsByEngineer(byEngineer)
                .build();
    }
}

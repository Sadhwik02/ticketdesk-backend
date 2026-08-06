package com.ticketdesk.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> ticketsByEngineer;
}

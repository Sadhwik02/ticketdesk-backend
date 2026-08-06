package com.ticketdesk.service;

import com.ticketdesk.dto.request.AssignTicketRequest;
import com.ticketdesk.dto.request.CreateTicketRequest;
import com.ticketdesk.dto.request.UpdateStatusRequest;
import com.ticketdesk.dto.request.UpdateTicketRequest;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    TicketResponse updateTicket(Long ticketId, UpdateTicketRequest request);

    void deleteTicket(Long ticketId);

    TicketResponse getTicketById(Long ticketId);

    TicketResponse getTicketByNumber(String ticketNumber);

    Page<TicketResponse> getAllTickets(Pageable pageable);

    Page<TicketResponse> getTicketsByStatus(TicketStatus status, Pageable pageable);

    Page<TicketResponse> getTicketsByPriority(TicketPriority priority, Pageable pageable);

    Page<TicketResponse> getTicketsByCategory(TicketCategory category, Pageable pageable);

    Page<TicketResponse> getTicketsByCurrentUser(Pageable pageable);

    TicketResponse assignEngineer(Long ticketId, AssignTicketRequest request);

    TicketResponse updateStatus(Long ticketId, UpdateStatusRequest request);
}

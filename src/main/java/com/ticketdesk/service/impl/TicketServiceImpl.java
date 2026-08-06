package com.ticketdesk.service.impl;

import com.ticketdesk.dto.request.AssignTicketRequest;
import com.ticketdesk.dto.request.CreateTicketRequest;
import com.ticketdesk.dto.request.UpdateStatusRequest;
import com.ticketdesk.dto.request.UpdateTicketRequest;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.entity.Ticket;
import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.exception.BadRequestException;
import com.ticketdesk.exception.InvalidStatusTransitionException;
import com.ticketdesk.exception.ResourceNotFoundException;
import com.ticketdesk.exception.UnauthorizedException;
import com.ticketdesk.mapper.TicketMapper;
import com.ticketdesk.repository.TicketRepository;
import com.ticketdesk.repository.UserRepository;
import com.ticketdesk.service.TicketService;
import com.ticketdesk.util.SecurityUtils;
import com.ticketdesk.util.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository      ticketRepository;
    private final UserRepository        userRepository;
    private final TicketMapper          ticketMapper;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final SecurityUtils         securityUtils;

    @Override
    @Transactional
    public TicketResponse createTicket(final CreateTicketRequest request) {
        final User currentUser = securityUtils.getCurrentUser();
        final long sequence    = ticketRepository.countByYear(Year.now().getValue()) + 1;
        final String ticketNum = ticketNumberGenerator.generate(sequence);

        final Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNum)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(TicketStatus.OPEN)
                .createdBy(currentUser)
                .build();

        final Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created: {} by user: {}", saved.getTicketNumber(), currentUser.getUsername());
        return ticketMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(final Long ticketId, final UpdateTicketRequest request) {
        final Ticket ticket = getTicketEntityById(ticketId);
        final User   current = securityUtils.getCurrentUser();

        // Only creator or admin can update
        if (!ticket.getCreatedBy().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You do not have permission to update this ticket");
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Closed tickets cannot be edited. Re-open the ticket first.");
        }

        if (request.getTitle() != null)       ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getCategory() != null)    ticket.setCategory(request.getCategory());
        if (request.getPriority() != null)    ticket.setPriority(request.getPriority());

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public void deleteTicket(final Long ticketId) {
        final Ticket ticket  = getTicketEntityById(ticketId);
        final User   current = securityUtils.getCurrentUser();

        if (!ticket.getCreatedBy().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You do not have permission to delete this ticket");
        }

        ticketRepository.delete(ticket);
        log.info("Ticket deleted: {} by user: {}", ticket.getTicketNumber(), current.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(final Long ticketId) {
        return ticketMapper.toResponse(getTicketEntityById(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(final String ticketNumber) {
        final Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(final Pageable pageable) {
        return ticketRepository.findAll(pageable).map(ticketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByStatus(final TicketStatus status, final Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(ticketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByPriority(final TicketPriority priority, final Pageable pageable) {
        return ticketRepository.findByPriority(priority, pageable).map(ticketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByCategory(final TicketCategory category, final Pageable pageable) {
        return ticketRepository.findByCategory(category, pageable).map(ticketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByCurrentUser(final Pageable pageable) {
        final User current = securityUtils.getCurrentUser();
        return ticketRepository.findByCreatedById(current.getId(), pageable).map(ticketMapper::toResponse);
    }

    @Override
    @Transactional
    public TicketResponse assignEngineer(final Long ticketId, final AssignTicketRequest request) {
        final Ticket ticket = getTicketEntityById(ticketId);
        final User engineer = userRepository.findById(request.getEngineerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getEngineerId()));

        if (engineer.getRole() != Role.SUPPORT_ENGINEER && engineer.getRole() != Role.ADMIN) {
            throw new BadRequestException("Assigned user must have SUPPORT_ENGINEER or ADMIN role");
        }
        if (!engineer.isActive()) {
            throw new BadRequestException("Cannot assign ticket to an inactive user");
        }

        ticket.setAssignedTo(engineer);

        // Auto-transition to IN_PROGRESS if OPEN
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        final Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket {} assigned to engineer: {}", ticket.getTicketNumber(), engineer.getUsername());
        return ticketMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(final Long ticketId, final UpdateStatusRequest request) {
        final Ticket ticket  = getTicketEntityById(ticketId);
        final User   current = securityUtils.getCurrentUser();

        final TicketStatus newStatus = request.getStatus();
        final TicketStatus curStatus = ticket.getStatus();

        if (!curStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(curStatus, newStatus);
        }

        // Role-based restrictions
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            if (current.getRole() == Role.EMPLOYEE) {
                throw new UnauthorizedException("Employees cannot mark tickets as RESOLVED or CLOSED");
            }
        }

        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        } else {
            ticket.setClosedAt(null); // Re-open clears closed date
        }

        final Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket {} status changed: {} -> {} by: {}",
                ticket.getTicketNumber(), curStatus, newStatus, current.getUsername());
        return ticketMapper.toResponse(saved);
    }

    // ===== Private Helpers =====

    private Ticket getTicketEntityById(final Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
    }
}

package com.ticketdesk.service;

import com.ticketdesk.dto.request.CreateTicketRequest;
import com.ticketdesk.dto.request.UpdateStatusRequest;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.entity.Ticket;
import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.exception.InvalidStatusTransitionException;
import com.ticketdesk.exception.ResourceNotFoundException;
import com.ticketdesk.mapper.TicketMapper;
import com.ticketdesk.repository.TicketRepository;
import com.ticketdesk.repository.UserRepository;
import com.ticketdesk.service.impl.TicketServiceImpl;
import com.ticketdesk.util.SecurityUtils;
import com.ticketdesk.util.TicketNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Unit Tests")
class TicketServiceTest {

    @Mock private TicketRepository      ticketRepository;
    @Mock private UserRepository        userRepository;
    @Mock private TicketMapper          ticketMapper;
    @Mock private TicketNumberGenerator ticketNumberGenerator;
    @Mock private SecurityUtils         securityUtils;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User currentUser;
    private Ticket ticket;
    private TicketResponse ticketResponse;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L).username("employee1").role(Role.EMPLOYEE).active(true)
                .firstName("Jane").lastName("Doe")
                .build();

        ticket = Ticket.builder()
                .id(1L).ticketNumber("TKT-2026-000001")
                .title("Test Ticket").description("Test description")
                .category(TicketCategory.SOFTWARE).priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN).createdBy(currentUser)
                .build();

        ticketResponse = TicketResponse.builder()
                .id(1L).ticketNumber("TKT-2026-000001")
                .status(TicketStatus.OPEN).build();
    }

    // ===== Create Ticket =====

    @Test
    @DisplayName("Should create ticket with correct ticket number")
    void createTicket_ShouldSucceed_WithValidRequest() {
        final CreateTicketRequest request = CreateTicketRequest.builder()
                .title("My Issue").description("Something is broken")
                .category(TicketCategory.SOFTWARE).priority(TicketPriority.HIGH)
                .build();

        given(securityUtils.getCurrentUser()).willReturn(currentUser);
        given(ticketRepository.countByYear(anyInt())).willReturn(0L);
        given(ticketNumberGenerator.generate(1L)).willReturn("TKT-2026-000001");
        given(ticketRepository.save(any(Ticket.class))).willReturn(ticket);
        given(ticketMapper.toResponse(any(Ticket.class))).willReturn(ticketResponse);

        final TicketResponse result = ticketService.createTicket(request);

        assertThat(result).isNotNull();
        assertThat(result.getTicketNumber()).isEqualTo("TKT-2026-000001");
        then(ticketRepository).should().save(any(Ticket.class));
    }

    // ===== Get Ticket =====

    @Test
    @DisplayName("Should return ticket when found by ID")
    void getTicketById_ShouldReturn_WhenTicketExists() {
        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket));
        given(ticketMapper.toResponse(ticket)).willReturn(ticketResponse);

        final TicketResponse result = ticketService.getTicketById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ticket not found")
    void getTicketById_ShouldThrow_WhenNotFound() {
        given(ticketRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket");
    }

    // ===== Status Transition =====

    @Test
    @DisplayName("Should update status when transition is valid")
    void updateStatus_ShouldSucceed_WithValidTransition() {
        final User engineer = User.builder().id(2L).role(Role.SUPPORT_ENGINEER).build();
        final UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS);

        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket));
        given(securityUtils.getCurrentUser()).willReturn(engineer);
        given(ticketRepository.save(any())).willReturn(ticket);
        given(ticketMapper.toResponse(any())).willReturn(ticketResponse);

        assertThatNoException().isThrownBy(() -> ticketService.updateStatus(1L, request));
        then(ticketRepository).should().save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException for invalid transition")
    void updateStatus_ShouldThrow_WhenTransitionInvalid() {
        // OPEN -> RESOLVED is not allowed
        final UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.RESOLVED);
        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket)); // ticket is OPEN
        given(securityUtils.getCurrentUser()).willReturn(currentUser);

        assertThatThrownBy(() -> ticketService.updateStatus(1L, request))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }
}

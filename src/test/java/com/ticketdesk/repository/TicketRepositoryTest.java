package com.ticketdesk.repository;

import com.ticketdesk.entity.Ticket;
import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.Year;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository slice tests using H2 in-memory DB.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("TicketRepository Tests")
class TicketRepositoryTest {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository   userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstName("Test").lastName("User")
                .username("testuser").email("test@example.com")
                .password("encoded").role(Role.EMPLOYEE).active(true)
                .build());
    }

    private Ticket buildTicket(final String suffix, final TicketStatus status) {
        return Ticket.builder()
                .ticketNumber("TKT-2026-" + suffix)
                .title("Ticket " + suffix).description("Description " + suffix)
                .category(TicketCategory.SOFTWARE).priority(TicketPriority.MEDIUM)
                .status(status).createdBy(user)
                .build();
    }

    @Test
    @DisplayName("Should find ticket by ticket number")
    void findByTicketNumber_ShouldReturn_WhenExists() {
        ticketRepository.save(buildTicket("000001", TicketStatus.OPEN));

        assertThat(ticketRepository.findByTicketNumber("TKT-2026-000001"))
                .isPresent()
                .get()
                .extracting(Ticket::getTitle)
                .isEqualTo("Ticket 000001");
    }

    @Test
    @DisplayName("Should count tickets by status")
    void countByStatus_ShouldReturnCorrectCount() {
        ticketRepository.save(buildTicket("000001", TicketStatus.OPEN));
        ticketRepository.save(buildTicket("000002", TicketStatus.OPEN));
        ticketRepository.save(buildTicket("000003", TicketStatus.IN_PROGRESS));

        assertThat(ticketRepository.countByStatus(TicketStatus.OPEN)).isEqualTo(2L);
        assertThat(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should filter tickets by status with pagination")
    void findByStatus_ShouldReturnPage() {
        ticketRepository.save(buildTicket("000001", TicketStatus.OPEN));
        ticketRepository.save(buildTicket("000002", TicketStatus.OPEN));
        ticketRepository.save(buildTicket("000003", TicketStatus.CLOSED));

        final Page<Ticket> page = ticketRepository.findByStatus(TicketStatus.OPEN, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should count tickets created in current year")
    void countByYear_ShouldReturnCount() {
        ticketRepository.save(buildTicket("000001", TicketStatus.OPEN));
        ticketRepository.save(buildTicket("000002", TicketStatus.OPEN));

        final long count = ticketRepository.countByYear(Year.now().getValue());
        assertThat(count).isGreaterThanOrEqualTo(2L);
    }
}

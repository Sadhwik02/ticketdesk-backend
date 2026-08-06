package com.ticketdesk.repository;

import com.ticketdesk.entity.Ticket;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    boolean existsByTicketNumber(String ticketNumber);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);

    Page<Ticket> findByCategory(TicketCategory category, Pageable pageable);

    Page<Ticket> findByCreatedById(Long userId, Pageable pageable);

    Page<Ticket> findByAssignedToId(Long engineerId, Pageable pageable);

    // ===== Dashboard counts =====

    long countByStatus(TicketStatus status);

    long countByPriority(TicketPriority priority);

    long countByCategory(TicketCategory category);

    @Query("""
            SELECT t.assignedTo.id AS engineerId,
                   t.assignedTo.firstName AS firstName,
                   t.assignedTo.lastName AS lastName,
                   COUNT(t) AS ticketCount
            FROM Ticket t
            WHERE t.assignedTo IS NOT NULL
            GROUP BY t.assignedTo.id, t.assignedTo.firstName, t.assignedTo.lastName
            """)
    List<Object[]> countTicketsByEngineer();

    // ===== Ticket-number sequence helper =====

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year")
    long countByYear(@Param("year") int year);
}

package com.ticketdesk.validation;

import com.ticketdesk.entity.Ticket;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic Ticket searches.
 */
public final class TicketSpecification {

    private TicketSpecification() {}

    public static Specification<Ticket> hasKeyword(final String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            final String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")),       like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("ticketNumber")), like)
            );
        };
    }

    public static Specification<Ticket> hasStatus(final TicketStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(final TicketPriority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasCategory(final TicketCategory category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Ticket> assignedToEngineer(final Long engineerId) {
        return (root, query, cb) ->
                engineerId == null ? null : cb.equal(root.get("assignedTo").get("id"), engineerId);
    }

    public static Specification<Ticket> createdBetween(final LocalDate startDate,
                                                         final LocalDate endDate) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }
            return predicates.isEmpty() ? null
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

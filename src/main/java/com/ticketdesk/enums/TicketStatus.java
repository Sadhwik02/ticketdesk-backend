package com.ticketdesk.enums;

import java.util.Set;

/**
 * Ticket lifecycle statuses with valid transition rules.
 */
public enum TicketStatus {

    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    /**
     * Returns allowed next statuses from the current state.
     * CLOSED tickets may be re-opened (back to OPEN).
     */
    public Set<TicketStatus> getAllowedTransitions() {
        return switch (this) {
            case OPEN        -> Set.of(IN_PROGRESS, CLOSED);
            case IN_PROGRESS -> Set.of(RESOLVED, CLOSED);
            case RESOLVED    -> Set.of(CLOSED, IN_PROGRESS);
            case CLOSED      -> Set.of(OPEN);    // re-open allowed
        };
    }

    public boolean canTransitionTo(final TicketStatus next) {
        return getAllowedTransitions().contains(next);
    }
}

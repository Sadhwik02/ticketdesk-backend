package com.ticketdesk.exception;

import com.ticketdesk.enums.TicketStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(final TicketStatus from, final TicketStatus to) {
        super(String.format("Invalid status transition from '%s' to '%s'", from.name(), to.name()));
    }
}

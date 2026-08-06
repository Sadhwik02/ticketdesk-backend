package com.ticketdesk.util;

import com.ticketdesk.constant.AppConstants;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Generates ticket numbers in the format TKT-YEAR-NNNNNN.
 */
@Component
public class TicketNumberGenerator {

    /**
     * Builds the ticket number for the given sequence within a year.
     *
     * @param sequence  The sequential count of tickets in the current year (1-based).
     * @return          Formatted ticket number, e.g. TKT-2026-000001
     */
    public String generate(final long sequence) {
        final int currentYear = Year.now().getValue();
        return String.format(AppConstants.TICKET_NUMBER_FORMAT,
                AppConstants.TICKET_NUMBER_PREFIX,
                currentYear,
                sequence);
    }
}

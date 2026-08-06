package com.ticketdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TicketDesk — Enterprise IT Support Ticket Management System.
 */
@SpringBootApplication
@EnableScheduling
public class TicketDeskApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TicketDeskApplication.class, args);
    }
}

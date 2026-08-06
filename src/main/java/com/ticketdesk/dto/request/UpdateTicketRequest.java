package com.ticketdesk.dto.request;

import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTicketRequest {

    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    private TicketCategory category;

    private TicketPriority priority;
}

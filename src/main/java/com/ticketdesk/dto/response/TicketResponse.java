package com.ticketdesk.dto.response;

import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private UserSummaryResponse createdBy;
    private UserSummaryResponse assignedTo;
    private AttachmentResponse attachment;
    private long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}

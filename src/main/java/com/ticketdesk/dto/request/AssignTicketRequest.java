package com.ticketdesk.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTicketRequest {

    @NotNull(message = "Engineer ID is required")
    @Positive(message = "Engineer ID must be a positive number")
    private Long engineerId;
}

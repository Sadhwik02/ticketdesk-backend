package com.ticketdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 5000, message = "Comment must be between 1 and 5000 characters")
    private String comment;
}

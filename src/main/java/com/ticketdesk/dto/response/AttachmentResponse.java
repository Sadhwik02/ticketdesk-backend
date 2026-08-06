package com.ticketdesk.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private Long size;
    private Long ticketId;
    private String ticketNumber;
    private UserSummaryResponse uploadedBy;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}

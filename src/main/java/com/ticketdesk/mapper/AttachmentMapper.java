package com.ticketdesk.mapper;

import com.ticketdesk.dto.response.AttachmentResponse;
import com.ticketdesk.entity.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for {@link Attachment} entity.
 */
@Component
@RequiredArgsConstructor
public class AttachmentMapper {

    private final UserMapper userMapper;

    public AttachmentResponse toResponse(final Attachment attachment) {
        if (attachment == null) return null;
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .contentType(attachment.getContentType())
                .size(attachment.getSize())
                .ticketId(attachment.getTicket().getId())
                .ticketNumber(attachment.getTicket().getTicketNumber())
                .uploadedBy(userMapper.toSummaryResponse(attachment.getUploadedBy()))
                .uploadedAt(attachment.getUploadedAt())
                .downloadUrl("/api/v1/attachments/" + attachment.getId() + "/download")
                .build();
    }
}

package com.ticketdesk.mapper;

import com.ticketdesk.dto.response.AttachmentResponse;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.entity.Ticket;
import com.ticketdesk.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for {@link Ticket} entity.
 */
@Component
@RequiredArgsConstructor
public class TicketMapper {

    private final UserMapper       userMapper;
    private final AttachmentMapper attachmentMapper;
    private final CommentRepository commentRepository;

    public TicketResponse toResponse(final Ticket ticket) {
        if (ticket == null) return null;

        final AttachmentResponse attachmentResp =
                ticket.getAttachment() != null
                        ? attachmentMapper.toResponse(ticket.getAttachment())
                        : null;

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .createdBy(userMapper.toSummaryResponse(ticket.getCreatedBy()))
                .assignedTo(userMapper.toSummaryResponse(ticket.getAssignedTo()))
                .attachment(attachmentResp)
                .commentCount(commentRepository.countByTicketId(ticket.getId()))
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }
}

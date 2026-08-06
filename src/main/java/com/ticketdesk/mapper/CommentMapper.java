package com.ticketdesk.mapper;

import com.ticketdesk.dto.response.CommentResponse;
import com.ticketdesk.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for {@link Comment} entity.
 */
@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserMapper userMapper;

    public CommentResponse toResponse(final Comment comment) {
        if (comment == null) return null;
        return CommentResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .ticketId(comment.getTicket().getId())
                .ticketNumber(comment.getTicket().getTicketNumber())
                .createdBy(userMapper.toSummaryResponse(comment.getCreatedBy()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

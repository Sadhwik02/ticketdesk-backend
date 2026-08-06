package com.ticketdesk.service.impl;

import com.ticketdesk.dto.request.CommentRequest;
import com.ticketdesk.dto.response.CommentResponse;
import com.ticketdesk.entity.Comment;
import com.ticketdesk.entity.Ticket;
import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import com.ticketdesk.exception.ResourceNotFoundException;
import com.ticketdesk.exception.UnauthorizedException;
import com.ticketdesk.mapper.CommentMapper;
import com.ticketdesk.repository.CommentRepository;
import com.ticketdesk.repository.TicketRepository;
import com.ticketdesk.service.CommentService;
import com.ticketdesk.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository  ticketRepository;
    private final CommentMapper     commentMapper;
    private final SecurityUtils     securityUtils;

    @Override
    @Transactional
    public CommentResponse addComment(final Long ticketId, final CommentRequest request) {
        final Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        final User current = securityUtils.getCurrentUser();

        final Comment comment = Comment.builder()
                .comment(request.getComment())
                .ticket(ticket)
                .createdBy(current)
                .build();

        final Comment saved = commentRepository.save(comment);
        log.debug("Comment added to ticket {} by {}", ticket.getTicketNumber(), current.getUsername());
        return commentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(final Long commentId, final CommentRequest request) {
        final Comment comment = getCommentEntityById(commentId);
        final User    current = securityUtils.getCurrentUser();

        if (!comment.getCreatedBy().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You do not have permission to edit this comment");
        }

        comment.setComment(request.getComment());
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(final Long commentId) {
        final Comment comment = getCommentEntityById(commentId);
        final User    current = securityUtils.getCurrentUser();

        if (!comment.getCreatedBy().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You do not have permission to delete this comment");
        }

        commentRepository.delete(comment);
        log.debug("Comment {} deleted by {}", commentId, current.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getTicketComments(final Long ticketId, final Pageable pageable) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket", "id", ticketId);
        }
        return commentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId, pageable)
                .map(commentMapper::toResponse);
    }

    private Comment getCommentEntityById(final Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    }
}

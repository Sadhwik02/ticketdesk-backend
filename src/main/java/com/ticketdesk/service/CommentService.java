package com.ticketdesk.service;

import com.ticketdesk.dto.request.CommentRequest;
import com.ticketdesk.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse addComment(Long ticketId, CommentRequest request);

    CommentResponse updateComment(Long commentId, CommentRequest request);

    void deleteComment(Long commentId);

    Page<CommentResponse> getTicketComments(Long ticketId, Pageable pageable);
}

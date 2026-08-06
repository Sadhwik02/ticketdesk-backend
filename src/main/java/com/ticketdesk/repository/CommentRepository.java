package com.ticketdesk.repository;

import com.ticketdesk.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTicketIdOrderByCreatedAtDesc(Long ticketId, Pageable pageable);

    long countByTicketId(Long ticketId);

    void deleteByTicketId(Long ticketId);
}

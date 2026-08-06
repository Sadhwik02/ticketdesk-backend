package com.ticketdesk.repository;

import com.ticketdesk.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    void deleteByTicketId(Long ticketId);
}

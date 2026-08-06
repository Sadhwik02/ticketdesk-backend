package com.ticketdesk.service;

import com.ticketdesk.dto.response.AttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file);

    AttachmentResponse replaceAttachment(Long ticketId, MultipartFile file);

    AttachmentResponse getAttachmentByTicketId(Long ticketId);

    AttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long ticketId);
}

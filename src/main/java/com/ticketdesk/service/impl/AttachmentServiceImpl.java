package com.ticketdesk.service.impl;

import com.ticketdesk.config.FileStorageConfig;
import com.ticketdesk.constant.AppConstants;
import com.ticketdesk.dto.response.AttachmentResponse;
import com.ticketdesk.entity.Attachment;
import com.ticketdesk.entity.Ticket;
import com.ticketdesk.entity.User;
import com.ticketdesk.exception.BadRequestException;
import com.ticketdesk.exception.FileStorageException;
import com.ticketdesk.exception.ResourceNotFoundException;
import com.ticketdesk.mapper.AttachmentMapper;
import com.ticketdesk.repository.AttachmentRepository;
import com.ticketdesk.repository.TicketRepository;
import com.ticketdesk.service.AttachmentService;
import com.ticketdesk.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository     ticketRepository;
    private final AttachmentMapper     attachmentMapper;
    private final FileStorageConfig    fileStorageConfig;
    private final SecurityUtils        securityUtils;

    @Override
    @Transactional
    public AttachmentResponse uploadAttachment(final Long ticketId, final MultipartFile file) {
        if (attachmentRepository.existsByTicketId(ticketId)) {
            throw new BadRequestException(
                    "Ticket already has an attachment. Use replace endpoint to update it.");
        }
        return doUpload(ticketId, file);
    }

    @Override
    @Transactional
    public AttachmentResponse replaceAttachment(final Long ticketId, final MultipartFile file) {
        attachmentRepository.findByTicketId(ticketId).ifPresent(existing -> {
            deleteFileFromDisk(existing.getFilePath());
            attachmentRepository.delete(existing);
        });
        return doUpload(ticketId, file);
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentByTicketId(final Long ticketId) {
        final Attachment att = attachmentRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "ticketId", ticketId));
        return attachmentMapper.toResponse(att);
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentById(final Long attachmentId) {
        return attachmentMapper.toResponse(getAttachmentEntityById(attachmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(final Long attachmentId) {
        final Attachment attachment = getAttachmentEntityById(attachmentId);
        try {
            final Path filePath = fileStorageConfig.getUploadPath().resolve(attachment.getFilePath()).normalize();
            final Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("File not found or not readable: " + attachment.getFileName());
            }
            return resource;
        } catch (final MalformedURLException ex) {
            throw new FileStorageException("Could not read file: " + attachment.getFileName(), ex);
        }
    }

    @Override
    @Transactional
    public void deleteAttachment(final Long ticketId) {
        final Attachment att = attachmentRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "ticketId", ticketId));

        deleteFileFromDisk(att.getFilePath());
        attachmentRepository.delete(att);
        log.info("Attachment deleted for ticket: {}", ticketId);
    }

    // ===== Private Helpers =====

    private AttachmentResponse doUpload(final Long ticketId, final MultipartFile file) {
        validateFile(file);

        final Ticket ticket  = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        final User currentUser = securityUtils.getCurrentUser();

        final String originalName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));
        final String extension    = getExtension(originalName);
        final String storedName   = UUID.randomUUID() + "." + extension;

        final Path targetLocation = fileStorageConfig.getUploadPath().resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException ex) {
            throw new FileStorageException("Failed to store file: " + originalName, ex);
        }

        final Attachment attachment = Attachment.builder()
                .fileName(originalName)
                .contentType(file.getContentType())
                .filePath(storedName)
                .size(file.getSize())
                .ticket(ticket)
                .uploadedBy(currentUser)
                .build();

        final Attachment saved = attachmentRepository.save(attachment);
        log.info("Attachment '{}' uploaded for ticket: {}", originalName, ticket.getTicketNumber());
        return attachmentMapper.toResponse(saved);
    }

    private void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        if (file.getSize() > AppConstants.MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds the maximum allowed 10MB");
        }

        final String contentType = file.getContentType();
        final boolean typeAllowed = Arrays.asList(AppConstants.ALLOWED_CONTENT_TYPES).contains(contentType);
        if (!typeAllowed) {
            throw new BadRequestException(
                    "File type '" + contentType + "' is not allowed. Allowed types: jpg, jpeg, png, pdf");
        }

        final String filename  = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));
        final String extension = getExtension(filename).toLowerCase();
        final boolean extAllowed = Arrays.asList(AppConstants.ALLOWED_EXTENSIONS).contains(extension);
        if (!extAllowed) {
            throw new BadRequestException(
                    "File extension '." + extension + "' is not allowed");
        }

        if (filename.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + filename);
        }
    }

    private String getExtension(final String filename) {
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) throw new BadRequestException("File has no extension: " + filename);
        return filename.substring(dotIndex + 1);
    }

    private void deleteFileFromDisk(final String storedPath) {
        try {
            final Path filePath = fileStorageConfig.getUploadPath().resolve(storedPath).normalize();
            Files.deleteIfExists(filePath);
        } catch (final IOException ex) {
            log.warn("Could not delete file from disk: {}", storedPath, ex);
        }
    }

    private Attachment getAttachmentEntityById(final Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
    }
}

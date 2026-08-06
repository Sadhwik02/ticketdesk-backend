package com.ticketdesk.controller;

import com.ticketdesk.dto.response.AttachmentResponse;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Attachments", description = "Upload, download, replace, and delete ticket attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "Upload an attachment for a ticket (one per ticket)")
    @PostMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable final Long ticketId,
            @RequestParam("file") final MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Attachment uploaded successfully",
                        attachmentService.uploadAttachment(ticketId, file)));
    }

    @Operation(summary = "Replace the attachment on a ticket")
    @PutMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> replaceAttachment(
            @PathVariable final Long ticketId,
            @RequestParam("file") final MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Attachment replaced successfully",
                attachmentService.replaceAttachment(ticketId, file)));
    }

    @Operation(summary = "Get attachment metadata by ticket ID")
    @GetMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getAttachmentByTicket(
            @PathVariable final Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Attachment retrieved",
                attachmentService.getAttachmentByTicketId(ticketId)));
    }

    @Operation(summary = "Get attachment metadata by attachment ID")
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getAttachmentById(
            @PathVariable final Long attachmentId) {
        return ResponseEntity.ok(ApiResponse.success("Attachment retrieved",
                attachmentService.getAttachmentById(attachmentId)));
    }

    @Operation(summary = "Download file by attachment ID")
    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable final Long attachmentId) {
        final AttachmentResponse meta     = attachmentService.getAttachmentById(attachmentId);
        final Resource           resource = attachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.getFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "Delete the attachment from a ticket")
    @DeleteMapping("/tickets/{ticketId}/attachments")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable final Long ticketId) {
        attachmentService.deleteAttachment(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully"));
    }
}

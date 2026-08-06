package com.ticketdesk.controller;

import com.ticketdesk.constant.AppConstants;
import com.ticketdesk.dto.request.CommentRequest;
import com.ticketdesk.dto.response.CommentResponse;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Comments", description = "Add, update, and delete comments on tickets")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Add a comment to a ticket")
    @PostMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable final Long ticketId,
            @Valid @RequestBody final CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Comment added successfully",
                        commentService.addComment(ticketId, request)));
    }

    @Operation(summary = "Get all comments for a ticket (newest first)")
    @GetMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getTicketComments(
            @PathVariable final Long ticketId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size) {
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved",
                commentService.getTicketComments(ticketId,
                        PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE)))));
    }

    @Operation(summary = "Update a comment")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable final Long commentId,
            @Valid @RequestBody final CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully",
                commentService.updateComment(commentId, request)));
    }

    @Operation(summary = "Delete a comment")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable final Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }
}

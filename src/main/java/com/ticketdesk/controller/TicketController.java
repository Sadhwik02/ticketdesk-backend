package com.ticketdesk.controller;

import com.ticketdesk.constant.AppConstants;
import com.ticketdesk.dto.request.AssignTicketRequest;
import com.ticketdesk.dto.request.CreateTicketRequest;
import com.ticketdesk.dto.request.UpdateStatusRequest;
import com.ticketdesk.dto.request.UpdateTicketRequest;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Ticket Management", description = "Full ticket lifecycle management")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Create a new support ticket")
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody final CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Ticket created successfully", ticketService.createTicket(request)));
    }

    @Operation(summary = "Get ticket by ID")
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(@PathVariable final Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved", ticketService.getTicketById(ticketId)));
    }

    @Operation(summary = "Get ticket by ticket number (e.g. TKT-2026-000001)")
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByNumber(
            @PathVariable final String ticketNumber) {
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved",
                ticketService.getTicketByNumber(ticketNumber)));
    }

    @Operation(summary = "Get all tickets with pagination and sorting")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY)     final String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR)    final String sortDir) {

        final Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved", ticketService.getAllTickets(pageable)));
    }

    @Operation(summary = "Get tickets created by the current user")
    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyTickets(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size) {
        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE));
        return ResponseEntity.ok(ApiResponse.success("Your tickets retrieved",
                ticketService.getTicketsByCurrentUser(pageable)));
    }

    @Operation(summary = "Filter tickets by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getByStatus(
            @PathVariable final TicketStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size) {
        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved",
                ticketService.getTicketsByStatus(status, pageable)));
    }

    @Operation(summary = "Filter tickets by priority")
    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getByPriority(
            @PathVariable final TicketPriority priority,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size) {
        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved",
                ticketService.getTicketsByPriority(priority, pageable)));
    }

    @Operation(summary = "Filter tickets by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getByCategory(
            @PathVariable final TicketCategory category,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size) {
        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved",
                ticketService.getTicketsByCategory(category, pageable)));
    }

    @Operation(summary = "Update ticket details")
    @PutMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @PathVariable final Long ticketId,
            @Valid @RequestBody final UpdateTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully",
                ticketService.updateTicket(ticketId, request)));
    }

    @Operation(summary = "Delete a ticket (Admin or ticket creator)")
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable final Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully"));
    }

    @Operation(summary = "Assign ticket to a support engineer (Admin/Engineer)")
    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignEngineer(
            @PathVariable final Long ticketId,
            @Valid @RequestBody final AssignTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully",
                ticketService.assignEngineer(ticketId, request)));
    }

    @Operation(summary = "Update ticket status with lifecycle validation")
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(
            @PathVariable final Long ticketId,
            @Valid @RequestBody final UpdateStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated successfully",
                ticketService.updateStatus(ticketId, request)));
    }
}

package com.ticketdesk.controller;

import com.ticketdesk.constant.AppConstants;
import com.ticketdesk.dto.response.TicketResponse;
import com.ticketdesk.enums.TicketCategory;
import com.ticketdesk.enums.TicketPriority;
import com.ticketdesk.enums.TicketStatus;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Search", description = "Advanced multi-criteria ticket search using JPA Specifications")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Search tickets with multiple optional filters")
    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> searchTickets(
            @Parameter(description = "Keyword to search in title, description, or ticket number")
            @RequestParam(required = false) final String keyword,

            @Parameter(description = "Filter by ticket status")
            @RequestParam(required = false) final TicketStatus status,

            @Parameter(description = "Filter by ticket priority")
            @RequestParam(required = false) final TicketPriority priority,

            @Parameter(description = "Filter by ticket category")
            @RequestParam(required = false) final TicketCategory category,

            @Parameter(description = "Filter by assigned engineer ID")
            @RequestParam(required = false) final Long engineerId,

            @Parameter(description = "Start date filter (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate startDate,

            @Parameter(description = "End date filter (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate endDate,

            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY)     final String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR)    final String sortDir) {

        final Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        final var pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);

        final Page<TicketResponse> results = searchService.searchTickets(
                keyword, status, priority, category, engineerId, startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success("Search completed. Found " + results.getTotalElements() + " tickets.", results));
    }
}

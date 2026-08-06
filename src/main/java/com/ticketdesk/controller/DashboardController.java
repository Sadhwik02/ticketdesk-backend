package com.ticketdesk.controller;

import com.ticketdesk.dto.response.DashboardResponse;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dashboard", description = "Ticket statistics and analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get full dashboard statistics (Admin and Support Engineer)")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved",
                dashboardService.getDashboardStats()));
    }
}

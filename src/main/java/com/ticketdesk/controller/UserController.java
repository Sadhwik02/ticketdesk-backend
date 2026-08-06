package com.ticketdesk.controller;

import com.ticketdesk.constant.AppConstants;
import com.ticketdesk.dto.request.RegisterRequest;
import com.ticketdesk.dto.request.UpdateUserRequest;
import com.ticketdesk.dto.response.UserResponse;
import com.ticketdesk.enums.Role;
import com.ticketdesk.response.ApiResponse;
import com.ticketdesk.service.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Management", description = "CRUD and management of system users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", userService.getCurrentUserProfile()));
    }

    @Operation(summary = "Create a new user (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody final RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User created successfully", userService.createUser(request)));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable final Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userService.getUserById(userId)));
    }

    @Operation(summary = "Get all users with pagination and sorting (Admin/Engineer)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY)     final String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR)    final String sortDir) {

        final Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userService.getAllUsers(pageable)));
    }

    @Operation(summary = "Search users by name, email, username, role, or active status")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final Role role,
            @RequestParam(required = false) final Boolean active,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) final int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE)   final int size) {

        final Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE));
        return ResponseEntity.ok(ApiResponse.success("Search completed",
                userService.searchUsers(search, role, active, pageable)));
    }

    @Operation(summary = "Update user (Admin only)")
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable final Long userId,
            @Valid @RequestBody final UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully",
                userService.updateUser(userId, request)));
    }

    @Operation(summary = "Delete user (Admin only)")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable final Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @Operation(summary = "Activate a deactivated user (Admin only)")
    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable final Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User activated successfully",
                userService.activateUser(userId)));
    }

    @Operation(summary = "Deactivate a user (Admin only)")
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable final Long userId) {
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully",
                userService.deactivateUser(userId)));
    }
}

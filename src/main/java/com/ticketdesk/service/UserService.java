package com.ticketdesk.service;

import com.ticketdesk.dto.request.RegisterRequest;
import com.ticketdesk.dto.request.UpdateUserRequest;
import com.ticketdesk.dto.response.UserResponse;
import com.ticketdesk.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(RegisterRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    void deleteUser(Long userId);

    UserResponse getUserById(Long userId);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> searchUsers(String search, Role role, Boolean active, Pageable pageable);

    UserResponse activateUser(Long userId);

    UserResponse deactivateUser(Long userId);

    UserResponse getCurrentUserProfile();
}

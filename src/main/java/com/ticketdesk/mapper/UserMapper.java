package com.ticketdesk.mapper;

import com.ticketdesk.dto.response.UserResponse;
import com.ticketdesk.dto.response.UserSummaryResponse;
import com.ticketdesk.entity.User;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for {@link User} entity.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(final User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserSummaryResponse toSummaryResponse(final User user) {
        if (user == null) return null;
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

package com.ticketdesk.service;

import com.ticketdesk.dto.request.RegisterRequest;
import com.ticketdesk.dto.request.UpdateUserRequest;
import com.ticketdesk.dto.response.UserResponse;
import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import com.ticketdesk.exception.BadRequestException;
import com.ticketdesk.exception.DuplicateResourceException;
import com.ticketdesk.exception.ResourceNotFoundException;
import com.ticketdesk.mapper.UserMapper;
import com.ticketdesk.repository.UserRepository;
import com.ticketdesk.service.impl.UserServiceImpl;
import com.ticketdesk.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper      userMapper;
    @Mock private SecurityUtils   securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private User targetUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).username("admin")
                .role(Role.ADMIN).active(true).build();
        targetUser = User.builder().id(2L).username("employee")
                .email("emp@test.com").role(Role.EMPLOYEE).active(true).build();
        userResponse = UserResponse.builder().id(2L).username("employee")
                .email("emp@test.com").role(Role.EMPLOYEE).build();
    }

    @Test
    @DisplayName("Should return user response when user found by ID")
    void getUserById_ShouldReturn_WhenExists() {
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(userMapper.toResponse(targetUser)).willReturn(userResponse);

        final UserResponse result = userService.getUserById(2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void getUserById_ShouldThrow_WhenNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should activate user successfully")
    void activateUser_ShouldSucceed_WhenUserInactive() {
        targetUser.setActive(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(userRepository.save(any())).willReturn(targetUser);
        given(userMapper.toResponse(any())).willReturn(userResponse);

        assertThatNoException().isThrownBy(() -> userService.activateUser(2L));
        then(userRepository).should().save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when activating already active user")
    void activateUser_ShouldThrow_WhenAlreadyActive() {
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser)); // active=true
        assertThatThrownBy(() -> userService.activateUser(2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already active");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating user with existing username")
    void createUser_ShouldThrow_WhenUsernameExists() {
        final RegisterRequest request = RegisterRequest.builder()
                .username("existing").email("new@test.com")
                .build();
        given(userRepository.existsByUsername("existing")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should prevent admin from deleting own account")
    void deleteUser_ShouldThrow_WhenDeletingSelf() {
        given(securityUtils.getCurrentUser()).willReturn(adminUser);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot delete your own account");
    }
}

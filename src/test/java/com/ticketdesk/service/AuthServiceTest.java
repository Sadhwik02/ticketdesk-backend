package com.ticketdesk.service;

import com.ticketdesk.dto.request.LoginRequest;
import com.ticketdesk.dto.request.RegisterRequest;
import com.ticketdesk.dto.response.AuthResponse;
import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import com.ticketdesk.exception.DuplicateResourceException;
import com.ticketdesk.mapper.UserMapper;
import com.ticketdesk.repository.RefreshTokenRepository;
import com.ticketdesk.repository.UserRepository;
import com.ticketdesk.security.CustomUserDetailsService;
import com.ticketdesk.security.JwtTokenProvider;
import com.ticketdesk.service.impl.AuthServiceImpl;
import com.ticketdesk.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository           userRepository;
    @Mock private RefreshTokenRepository   refreshTokenRepository;
    @Mock private PasswordEncoder          passwordEncoder;
    @Mock private AuthenticationManager    authenticationManager;
    @Mock private JwtTokenProvider         jwtTokenProvider;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private UserMapper               userMapper;
    @Mock private SecurityUtils            securityUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs",  900000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    // ===== Register =====

    @Test
    @DisplayName("Should register user successfully when username and email are unique")
    void register_ShouldSucceed_WhenUsernameAndEmailUnique() {
        final RegisterRequest request = buildRegisterRequest();
        final User savedUser = buildUser();

        given(userRepository.existsByUsername(request.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(userDetailsService.loadUserByUsername(anyString()))
                .willReturn(buildUserDetails(savedUser));
        given(jwtTokenProvider.generateToken(any())).willReturn("mock-access-token");
        given(refreshTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userMapper.toResponse(any())).willReturn(null);

        final AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username already exists")
    void register_ShouldThrow_WhenUsernameTaken() {
        final RegisterRequest request = buildRegisterRequest();
        given(userRepository.existsByUsername(request.getUsername())).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already registered")
    void register_ShouldThrow_WhenEmailTaken() {
        final RegisterRequest request = buildRegisterRequest();
        given(userRepository.existsByUsername(request.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
    }

    // ===== Login =====

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_ShouldSucceed_WithValidCredentials() {
        final LoginRequest request = new LoginRequest("testuser", "Password@1");
        final User user = buildUser();

        given(authenticationManager.authenticate(any())).willReturn(null);
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(userDetailsService.loadUserByUsername(anyString()))
                .willReturn(buildUserDetails(user));
        given(jwtTokenProvider.generateToken(any())).willReturn("access-token");
        given(refreshTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userMapper.toResponse(any())).willReturn(null);

        final AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        then(refreshTokenRepository).should().revokeAllUserTokens(user);
    }

    // ===== Private Helpers =====

    private RegisterRequest buildRegisterRequest() {
        return RegisterRequest.builder()
                .firstName("John").lastName("Doe")
                .username("johndoe").email("john@example.com")
                .password("Password@1").role(Role.EMPLOYEE)
                .build();
    }

    private User buildUser() {
        return User.builder()
                .id(1L).firstName("John").lastName("Doe")
                .username("johndoe").email("john@example.com")
                .password("encoded").role(Role.EMPLOYEE).active(true)
                .build();
    }

    private org.springframework.security.core.userdetails.UserDetails buildUserDetails(final User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}

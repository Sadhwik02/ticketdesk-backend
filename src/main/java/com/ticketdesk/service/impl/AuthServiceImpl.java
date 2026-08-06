package com.ticketdesk.service.impl;

import com.ticketdesk.constant.AppConstants;
import com.ticketdesk.dto.request.*;
import com.ticketdesk.dto.response.AuthResponse;
import com.ticketdesk.dto.response.UserResponse;
import com.ticketdesk.entity.RefreshToken;
import com.ticketdesk.entity.User;
import com.ticketdesk.exception.BadRequestException;
import com.ticketdesk.exception.DuplicateResourceException;
import com.ticketdesk.exception.ResourceNotFoundException;
import com.ticketdesk.mapper.UserMapper;
import com.ticketdesk.repository.RefreshTokenRepository;
import com.ticketdesk.repository.UserRepository;
import com.ticketdesk.security.CustomUserDetailsService;
import com.ticketdesk.security.JwtTokenProvider;
import com.ticketdesk.service.AuthService;
import com.ticketdesk.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link AuthService} implementation handling registration, login,
 * token refresh, logout, and password management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository           userRepository;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final PasswordEncoder          passwordEncoder;
    private final AuthenticationManager    authenticationManager;
    private final JwtTokenProvider         jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper               userMapper;
    private final SecurityUtils            securityUtils;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    // ===== Registration =====

    @Override
    @Transactional
    public AuthResponse register(final RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already registered");
        }

        final User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .department(request.getDepartment())
                .role(request.getRole())
                .active(true)
                .build();

        final User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return buildAuthResponse(savedUser);
    }

    // ===== Login =====

    @Override
    @Transactional
    public AuthResponse login(final LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(), request.getPassword()));

        final User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username/email", request.getUsernameOrEmail()));

        // Revoke existing refresh tokens
        refreshTokenRepository.revokeAllUserTokens(user);

        log.info("User logged in successfully: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    // ===== Token Refresh =====

    @Override
    @Transactional
    public AuthResponse refreshToken(final RefreshTokenRequest request) {
        final RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token not found or invalid"));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }

        final User user = refreshToken.getUser();
        // Revoke old token and issue new pair
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    // ===== Logout =====

    @Override
    @Transactional
    public void logout(final String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("User logged out, token revoked for user: {}", token.getUser().getUsername());
        });
    }

    // ===== Change Password =====

    @Override
    @Transactional
    public void changePassword(final ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        final User user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all tokens to force re-login
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    // ===== Forgot Password (token-based) =====

    @Override
    @Transactional
    public void forgotPassword(final ForgotPasswordRequest request) {
        final User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // In a real app, send email with reset link.
        // Here we store a reset token as a refresh token with short expiry.
        final String resetToken = UUID.randomUUID().toString();
        final RefreshToken token = RefreshToken.builder()
                .token("RESET-" + resetToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(AppConstants.PASSWORD_RESET_TOKEN_EXPIRY_MS))
                .build();
        refreshTokenRepository.save(token);

        // TODO: send email with reset link containing token
        log.info("Password reset token generated for user: {} (token would be emailed)", user.getEmail());
    }

    // ===== Reset Password =====

    @Override
    @Transactional
    public void resetPassword(final ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        final String tokenValue = "RESET-" + request.getToken();
        final RefreshToken resetToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isRevoked() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token is expired or has already been used");
        }

        final User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setRevoked(true);
        refreshTokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    // ===== Private Helpers =====

    private AuthResponse buildAuthResponse(final User user) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String accessToken  = jwtTokenProvider.generateToken(userDetails);
        final String refreshTokenStr = createRefreshToken(user);
        final UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.of(accessToken, refreshTokenStr, accessTokenExpirationMs / 1000, userResponse);
    }

    private String createRefreshToken(final User user) {
        final String tokenValue = UUID.randomUUID().toString();
        final RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(token);
        return tokenValue;
    }
}

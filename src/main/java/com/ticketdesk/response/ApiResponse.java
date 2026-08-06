package com.ticketdesk.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response envelope for every endpoint.
 *
 * @param <T> Type of the data payload.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;

    private String message;

    private T data;

    // ===== Static factory helpers =====

    public static <T> ApiResponse<T> success(final String message, final T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(final String message, final T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(final String message) {
        return ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .data(null)
                .build();
    }
}

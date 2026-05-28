package com.farmared.opsintelligence.dto.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        LocalDateTime timestamp,
        int status,
        String message,
        String path,
        T data,
        Object errors
) {

    public static <T> ApiResponse<T> success(HttpStatus status, String message, String path, T data) {
        return new ApiResponse<>(
                true,
                LocalDateTime.now(),
                status.value(),
                message,
                path,
                data,
                null
        );
    }

    public static ApiResponse<Object> error(HttpStatus status, String message, String path, Object errors) {
        return new ApiResponse<>(
                false,
                LocalDateTime.now(),
                status.value(),
                message,
                path,
                null,
                errors
        );
    }
}

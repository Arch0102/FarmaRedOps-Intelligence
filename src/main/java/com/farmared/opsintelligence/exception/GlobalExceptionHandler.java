package com.farmared.opsintelligence.exception;

import com.farmared.opsintelligence.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Recurso duplicado",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({
            BusinessRuleException.class,
            BadRequestException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleBusinessRule(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Solicitud invalida",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({
            UnauthorizedException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "No autorizado",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({
            ForbiddenException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleForbidden(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(
            InvalidTokenException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Token invalido",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST,
                "Uno o mas campos no cumplen las validaciones requeridas",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = "El parametro '" + exception.getName() + "' tiene un formato invalido";

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Parametro invalido",
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ocurrio un error inesperado en el sistema",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path
    ) {
        ApiResponse<Object> response = ApiResponse.error(status, message, path, List.of(error));

        return ResponseEntity.status(status).body(response);
    }
}

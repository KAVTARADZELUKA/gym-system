package com.gym.gymsystem.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.gymsystem.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        ErrorResponse errorResponse;

        if (authException instanceof UsernameNotFoundException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            errorResponse = createErrorResponse(HttpStatus.NOT_FOUND, "User not found", authException);
        } else if (authException instanceof BadCredentialsException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            errorResponse = createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", authException);
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            errorResponse = createErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed: " + authException.getMessage(), authException);
        }

        response.getWriter().write(convertToJson(errorResponse));
    }

    private ErrorResponse createErrorResponse(HttpStatus status, String message, AuthenticationException authException) {
        return new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                createErrorDetails(authException)
        );
    }

    private Map<String, String> createErrorDetails(AuthenticationException authException) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", authException.getMessage());
        return errors;
    }

    private String convertToJson(ErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}

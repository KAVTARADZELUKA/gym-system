package com.gym.gymsystem.filter;

import com.gym.gymsystem.exception.InvalidCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;
import com.gym.gymsystem.entity.User;
import com.gym.gymsystem.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CredentialsValidationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CredentialsValidationFilter.class);
    private final UserService userService;

    private static final List<String> EXEMPTED_URIS = Arrays.asList(
            "/api/trainee",
            "/api/trainer",
            "/api/training-types",
            "/api/auth/login",
            "/api/auth/change-password"
    );

    public CredentialsValidationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (!EXEMPTED_URIS.contains(requestURI)) {
            String username = request.getHeader("username");
            String password = request.getHeader("password");

            if (username == null || password == null) {
                logger.error("Username and password are required");

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Username and password are required\"}");
                response.getWriter().flush();
                return;
            }

            Optional<User> userOpt = userService.getByUsername(username);
            if (userOpt.isEmpty() || !userService.usernamePasswordMatches(username, password)) {
                logger.error("Invalid credentials for username: {}", username);

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid credentials\"}");
                response.getWriter().flush();
                return;
            }

            User user = userOpt.get();
            if (!user.getIsActive()) {
                logger.error("Inactive user for username: {}", username);

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Inactive user\"}");
                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

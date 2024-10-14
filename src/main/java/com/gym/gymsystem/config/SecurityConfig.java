package com.gym.gymsystem.config;

import com.gym.gymsystem.filter.JwtAuthenticationFilter;
import com.gym.gymsystem.security.CustomAuthenticationEntryPoint;
import com.gym.gymsystem.security.CustomAuthenticationProvider;
import com.gym.gymsystem.service.CustomUserDetailsService;
import com.gym.gymsystem.service.TokenBlacklistService;
import com.gym.gymsystem.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint entryPoint;
    private final CustomAuthenticationProvider authenticationProvider;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(CustomAuthenticationEntryPoint entryPoint,
                          @Lazy CustomAuthenticationProvider authenticationProvider, JwtTokenUtil jwtTokenUtil, CustomUserDetailsService customUserDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.entryPoint = entryPoint;
        this.authenticationProvider = authenticationProvider;
        this.jwtTokenUtil = jwtTokenUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/trainee").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/trainer").permitAll()
                        .requestMatchers( "/api/auth/change-password").permitAll()

                        .requestMatchers(HttpMethod.PUT,"/api/trainee/*").hasAnyRole("ADMIN", "TRAINEE")
                        .requestMatchers(HttpMethod.DELETE,"/api/trainee/*").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,"/api/trainee/status/*").hasAnyRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,"/api/trainer/*").hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.PUT,"/api/trainer/*").hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.PATCH,"/api/trainer/status/*").hasAnyRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT,"/api/training/trainers").hasAnyRole("ADMIN", "TRAINEE")
                        .requestMatchers(HttpMethod.GET,"/api/training/trainer").hasAnyRole("ADMIN", "TRAINER")
                        .requestMatchers(HttpMethod.POST,"/api/training").hasAnyRole("ADMIN", "TRAINER")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(entryPoint)
                )
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtRequestFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, customUserDetailsService, tokenBlacklistService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

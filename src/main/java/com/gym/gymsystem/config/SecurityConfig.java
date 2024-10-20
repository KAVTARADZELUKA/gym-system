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

import static com.gym.gymsystem.dto.user.Role.*;

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
                        .requestMatchers(HttpMethod.POST, "/trainee").permitAll()
                        .requestMatchers(HttpMethod.POST, "/trainer").permitAll()
                        .requestMatchers( "/auth/change-password").permitAll()
                        .requestMatchers( "/training-types").permitAll()

                        .requestMatchers(HttpMethod.PUT,"/trainee/*").hasAnyRole(ADMIN.getRole(), TRAINEE.getRole())
                        .requestMatchers(HttpMethod.DELETE,"/trainee/*").hasAnyRole(ADMIN.getRole())
                        .requestMatchers(HttpMethod.PATCH,"/trainee/status/*").hasAnyRole(ADMIN.getRole())

                        .requestMatchers(HttpMethod.GET,"/trainer/*").hasAnyRole(ADMIN.getRole(), TRAINER.getRole())
                        .requestMatchers(HttpMethod.PUT,"/trainer/*").hasAnyRole(ADMIN.getRole(), TRAINER.getRole())
                        .requestMatchers(HttpMethod.PATCH,"/trainer/status/*").hasAnyRole(ADMIN.getRole())

                        .requestMatchers(HttpMethod.PUT,"/training/trainee/*").hasAnyRole(ADMIN.getRole(), TRAINEE.getRole())
                        .requestMatchers(HttpMethod.GET,"/training/trainer/*").hasAnyRole(ADMIN.getRole(), TRAINER.getRole())
                        .requestMatchers(HttpMethod.POST,"/training").hasAnyRole(ADMIN.getRole(), TRAINER.getRole())

                        .requestMatchers("/actuator").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
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

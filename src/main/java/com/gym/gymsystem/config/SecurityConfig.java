package com.gym.gymsystem.config;

import com.gym.gymsystem.security.CustomAuthenticationEntryPoint;
import com.gym.gymsystem.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint entryPoint;
    private final CustomAuthenticationProvider authenticationProvider;

    public SecurityConfig(CustomAuthenticationEntryPoint entryPoint,
                          @Lazy CustomAuthenticationProvider authenticationProvider) {
        this.entryPoint = entryPoint;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(entryPoint)
                )
                .authenticationProvider(authenticationProvider);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

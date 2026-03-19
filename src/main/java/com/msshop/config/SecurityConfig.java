package com.msshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the current iteration.
 *
 * TODO (next iteration): Replace permitAll() with form-login for /admin/** paths.
 *       spring-boot-starter-security dependency is already declared in build.gradle.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf
                // Allow H2 console (uses frames) and all form POSTs for now
                .ignoringRequestMatchers("/h2-console/**", "/admin/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())); // H2 console needs frames
        return http.build();
    }
}

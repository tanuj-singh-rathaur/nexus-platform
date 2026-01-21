package com.rathaur.nexus.portfolioservice.config;

import com.rathaur.nexus.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Critical for REST APIs
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC: Viewing profiles is open to everyone
                        .requestMatchers(HttpMethod.GET, "/api/portfolio/profiles/{username}").permitAll()

                        // 2. PUBLIC: Infrastructure & Documentation
                        .requestMatchers("/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 3. SECURE: Portfolio Content (Projects, Skills, etc.)
                        // We explicitly protect the base path here
                        .requestMatchers("/api/portfolio/content/**").authenticated()

                        // 4. SECURE: Profile Management (Me endpoints)
                        .requestMatchers("/api/portfolio/profiles/me/**").authenticated()

                        // 5. CATCH-ALL: Anything else must be authenticated
                        .anyRequest().authenticated()
                )
                // Add the JWT filter to populate the SecurityContext with ROLE_USER
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
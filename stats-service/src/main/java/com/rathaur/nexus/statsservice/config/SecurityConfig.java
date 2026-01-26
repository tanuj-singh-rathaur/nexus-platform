package com.rathaur.nexus.statsservice.config;

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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC: Leaderboard and viewing specific user stats
                        .requestMatchers(HttpMethod.GET, "/api/v1/stats/leaderboard").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stats/{username}").permitAll()

                        // 2. PUBLIC: Swagger & Actuator
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()

                        // 3. SECURE: Syncing and "Me" data
                        .requestMatchers("/api/v1/stats/sync/**").authenticated()
                        .requestMatchers("/api/v1/stats/me").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
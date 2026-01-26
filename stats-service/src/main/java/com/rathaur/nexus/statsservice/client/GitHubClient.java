package com.rathaur.nexus.statsservice.client;

import com.rathaur.nexus.statsservice.exception.StatsDomainExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Optimized GitHub GraphQL Client with built-in error handling and security.
 * * @author Tanuj Singh Rathaur
 */
@Slf4j
@Component
public class GitHubClient {

    private final WebClient webClient;

    @Value("${github.api.token}")
    private String githubToken;

    // Use the builder to set base URL and common headers once
    public GitHubClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<Map<String, Object>> fetchUserStats(String githubUsername) {
        // Use variables instead of String formatting to prevent GraphQL injection
        String query = """
            query($username: String!) {
              user(login: $username) {
                repositories(first: 100, ownerAffiliations: OWNER) {
                  totalCount
                  nodes {
                    stargazerCount
                  }
                }
                contributionsCollection {
                  contributionCalendar {
                    totalContributions
                  }
                }
              }
            }
            """;

        return webClient.post()
                .uri("/graphql")
                .header("Authorization", "Bearer " + githubToken)
                .bodyValue(Map.of(
                        "query", query,
                        "variables", Map.of("username", githubUsername)
                ))
                .retrieve()
                // --- 1. DETECT RATE LIMITS (429) ---
                .onStatus(status -> status.value() == 429, response ->
                        Mono.error(new StatsDomainExceptions.ExternalProviderThrottledException(
                                "GitHub API limit reached. Cooling down...")))

                // --- 2. DETECT API ERRORS (4xx / 5xx) ---
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new StatsDomainExceptions.SyncServiceException("GitHub API call failed for " + githubUsername)))

                // --- 3. TYPE-SAFE DESERIALIZATION ---
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    if (data == null || data.get("user") == null) {
                        throw new StatsDomainExceptions.DataParsingException("GitHub user not found or empty data: " + githubUsername);
                    }
                    return data;
                });
    }
}
package com.rathaur.nexus.statsservice.client;

import com.rathaur.nexus.statsservice.exception.StatsDomainExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * LeetCode GraphQL Client.
 * Optimized for public profile scraping with rate-limit detection.
 * * @author Tanuj Singh Rathaur
 */
@Slf4j
@Component
public class LeetCodeClient {

    private final WebClient webClient;

    public LeetCodeClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://leetcode.com")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Referer", "https://leetcode.com") // Often required by LeetCode
                .build();
    }

    public Mono<Map<String, Object>> fetchUserStats(String leetcodeUsername) {
        String query = """
            query getUserProfile($username: String!) {
              matchedUser(username: $username) {
                profile {
                  ranking
                }
                submitStats: submitStatsGlobal {
                  acSubmissionNum {
                    difficulty
                    count
                  }
                }
              }
            }
            """;

        return webClient.post()
                .uri("/graphql")
                .bodyValue(Map.of(
                        "query", query,
                        "variables", Map.of("username", leetcodeUsername)
                ))
                .retrieve()
                // --- 1. DETECT LEETCODE THROTTLING (429) ---
                .onStatus(status -> status.value() == 429, response ->
                        Mono.error(new StatsDomainExceptions.ExternalProviderThrottledException(
                                "LeetCode rate limit hit for user: " + leetcodeUsername)))

                // --- 2. DETECT API ERRORS (4xx / 5xx) ---
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new StatsDomainExceptions.SyncServiceException("LeetCode API unreachable or user not found")))

                // --- 3. TYPE-SAFE DESERIALIZATION ---
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");

                    // LeetCode returns null 'matchedUser' if the username doesn't exist
                    if (data == null || data.get("matchedUser") == null) {
                        log.warn("LEETCODE-CLIENT: No data found for username: {}", leetcodeUsername);
                        throw new StatsDomainExceptions.DataParsingException("LeetCode user not found: " + leetcodeUsername);
                    }
                    return data;
                });
    }
}
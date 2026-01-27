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
 * LeetCode Synchronous Client.
 */
@Slf4j
@Component
public class LeetCodeClient {

    private final WebClient webClient;

    public LeetCodeClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://leetcode.com")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Referer", "https://leetcode.com")
                .build();
    }

    public Map<String, Object> fetchUserStats(String leetcodeUsername) {
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

        log.info("LEETCODE-CLIENT: [SYNC] Preparing query for {}", leetcodeUsername);

        try {
            // Using .block() to make the reactive call synchronous for debugging
            Map<String, Object> response = webClient.post()
                    .uri("/graphql")
                    .bodyValue(Map.of(
                            "query", query,
                            "variables", Map.of("username", leetcodeUsername)
                    ))
                    .retrieve()
                    .onStatus(status -> status.value() == 429, res ->
                            res.createException().flatMap(e -> Mono.error(new StatsDomainExceptions.ExternalProviderThrottledException("Throttled"))))
                    .onStatus(HttpStatusCode::isError, res ->
                            res.createException().flatMap(e -> Mono.error(new StatsDomainExceptions.SyncServiceException("API Error"))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // <--- BLOCKING HERE

            if (response == null || response.get("data") == null) {
                throw new StatsDomainExceptions.DataParsingException("LeetCode returned empty data");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data.get("matchedUser") == null) {
                throw new StatsDomainExceptions.DataParsingException("LeetCode user not found: " + leetcodeUsername);
            }

            log.info("LEETCODE-CLIENT: Data fetched successfully for {}", leetcodeUsername);
            return data;

        } catch (Exception e) {
            log.error("LEETCODE-CLIENT: Error during fetch: {}", e.getMessage(), e);
            throw e;
        }
    }
}
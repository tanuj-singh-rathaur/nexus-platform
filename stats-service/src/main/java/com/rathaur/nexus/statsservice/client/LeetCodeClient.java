package com.rathaur.nexus.statsservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class LeetCodeClient {

    private final WebClient webClient;

    public LeetCodeClient(WebClient.Builder builder) {
        // LeetCode's public GraphQL endpoint
        this.webClient = builder.baseUrl("https://leetcode.com/graphql").build();
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
                .bodyValue(Map.of(
                        "query", query,
                        "variables", Map.of("username", leetcodeUsername)
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (Map<String, Object>) response.get("data"));
    }
}
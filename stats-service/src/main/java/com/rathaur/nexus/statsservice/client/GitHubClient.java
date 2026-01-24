package com.rathaur.nexus.statsservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/23/2026
 */

@Component
public class GitHubClient {

    private final WebClient webClient;

    @Value("${github.api.token}")
    private String githubToken;

    public GitHubClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.github.com/graphql").build();
    }

    public Mono<Map<String, Object>> fetchUserStats(String githubUsername) {
        // Futuristic GraphQL query to get repos, stars, and languages in ONE call
        String query = """
            {
              user(login: "%s") {
                repositories(first: 100, ownerAffiliations: OWNER) {
                  totalCount
                  nodes {
                    stargazerCount
                    languages(first: 3, orderBy: {field: SIZE, direction: DESC}) {
                      nodes { name }
                    }
                  }
                }
                contributionsCollection {
                  contributionCalendar {
                    totalContributions
                  }
                }
              }
            }
            """.formatted(githubUsername);

        return webClient.post()
                .header("Authorization", "Bearer " + githubToken)
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(Map.class) // Non-blocking: returns a "Promise" of a Map
                .map(response -> (Map<String, Object>) response.get("data"));
    }
}
package com.rathaur.nexus.statsservice.client;

import com.rathaur.nexus.statsservice.exception.StatsDomainExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
public class GitHubClient {

    private final WebClient webClient;

    @Value("${github.api.token}")
    private String githubToken;

    public GitHubClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Map<String, Object> fetchUserStats(String githubUsername) {
        log.info("NEXUS-SYNC: [BLOCKING] Starting GitHub fetch for: {}", githubUsername);

        // 1. Manual Date Calculation
        Instant now = Instant.now();
        String toDate = now.toString();
        String fromDate = now.minus(7, ChronoUnit.DAYS).toString();

        String query = getQuery();

        // 2. Prepare Request Body
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", githubUsername);
        variables.put("fromDate", fromDate);
        variables.put("toDate", toDate);

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("variables", variables);

        // 3. Blocking Execution
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/graphql")
                    .header("Authorization", "Bearer " + githubToken)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> {
                        log.error("GITHUB-API-ERROR: Status Code {}", res.statusCode());
                        return res.createException();
                    })
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // FORCING BLOCKING BEHAVIOR

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("Empty response from GitHub");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> userNode = (Map<String, Object>) data.get("user");

            if (userNode == null) {
                throw new RuntimeException("User node not found in GitHub response");
            }

            log.info("NEXUS-SYNC: Successfully retrieved data for {}", githubUsername);
            return Map.of("user", userNode);

        } catch (Exception e) {
            log.error("NEXUS-SYNC: CRITICAL FAILURE in GitHub sync: {}", e.getMessage(), e);
            throw e; // This will now show the REAL stack trace in your console
        }
    }

    private String getQuery() {
        return """
        query($username: String!, $fromDate: DateTime!, $toDate: DateTime!) {
          user(login: $username) {
            name bio company location avatarUrl websiteUrl
            socialAccounts(first: 5) { nodes { provider url } }
            followers { totalCount }
            following { totalCount }
        
            # --- ALIAS: allRepos to get the total 16 ---
            allRepos: repositories(ownerAffiliations: OWNER) {
              totalCount
            }

            contributionsCollection(from: $fromDate, to: $toDate) {
              totalCommitContributions
              totalPullRequestReviewContributions
              totalIssueContributions
              contributionCalendar {
                totalContributions
                weeks { contributionDays { contributionCount date } }
              }
            }

            # --- ALIAS: recentRepos for the nodes list ---
            recentRepos: repositories(first: 7, ownerAffiliations: OWNER, orderBy: {field: PUSHED_AT, direction: DESC}) {
              nodes {
                name stargazerCount pushedAt
                primaryLanguage { name color }
                defaultBranchRef {
                  target {
                    ... on Commit {
                      history(first: 1) { nodes { message committedDate } }
                    }
                  }
                }
              }
            }
            
            pinnedItems(first: 6, types: REPOSITORY) {
              nodes {
                ... on Repository {
                  name description stargazerCount forkCount
                  primaryLanguage { name color }
                }
              }
            }
          }
        }
        """;
    }
}
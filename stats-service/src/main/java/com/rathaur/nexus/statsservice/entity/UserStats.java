package com.rathaur.nexus.statsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/22/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Version
    private long version;

    @Column(name = "nexus_score", nullable = false)
    private Double nexusScore;

    private Integer globalRank;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quick_summary", columnDefinition = "jsonb")
    private Map<String, Object> quickSummary;

    @OneToOne(mappedBy = "userStats", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private GitHubStats githubStats;

    @OneToOne(mappedBy = "userStats", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private LeetCodeStats leetCodeStats;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_sync_all")
    private Instant lastSyncAll;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.nexusScore == null) this.nexusScore = 0.0;
    }
}

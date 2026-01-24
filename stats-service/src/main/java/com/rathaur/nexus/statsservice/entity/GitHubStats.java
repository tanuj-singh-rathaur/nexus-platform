package com.rathaur.nexus.statsservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "github_stats")
public class GitHubStats {

    @Id
    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", nullable = false)
    @JsonIgnore
    private UserStats userStats;

    @Column(name = "total_stars")
    private int totalStars;

    @Column(name = "public_repos")
    private int publicRepos;

    @Column(name = "total_commits_yearly")
    private int totalCommitsYearly;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "telemetry", columnDefinition = "jsonb")
    private Map<String, Object> telemetry;

    @Column(name = "last_synced")
    private Instant lastSynced;

    @PrePersist
    @PreUpdate
    void onSync() {
        this.lastSynced = Instant.now();
    }
}

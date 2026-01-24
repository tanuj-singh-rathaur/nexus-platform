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
@Table(name = "leetcode_stats")
public class LeetCodeStats {

    @Id
    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", nullable = false)
    @JsonIgnore
    private UserStats userStats;

    private Integer globalRanking;
    private Integer totalSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "advanced_metrics", columnDefinition = "jsonb")
    private Map<String, Object> advancedMetrics;

    @Column(name = "last_synced")
    private Instant lastSynced;

    @PrePersist
    @PreUpdate
    void onSync() {
        this.lastSynced = Instant.now();
    }
}

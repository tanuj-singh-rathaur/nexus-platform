package com.rathaur.nexus.portfolioservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@Table(name = "projects") // Explicit table name
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {

    private String title;

    @Column(length = 2000)
    private String description;

    private String githubUrl;
    private String liveDemoUrl;
    private String imageUrl; // For a screenshot

    private boolean isFeatured; // "Top 3 Projects" flag
    private int displayOrder;   // To sort them manually

    @ElementCollection
    private List<String> techStack;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private Profile profile;
}
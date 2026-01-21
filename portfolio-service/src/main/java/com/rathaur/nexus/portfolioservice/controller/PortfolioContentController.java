package com.rathaur.nexus.portfolioservice.controller;

import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.portfolioservice.entity.*;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing portfolio sub-content (Projects, Skills, etc.).
 * Uses 'Authentication' to ensure users can only modify their own data.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@RestController
@RequestMapping("/api/portfolio/content")
@Observed(name = "portfolio.content.controller")
public class PortfolioContentController {

    private final ProfileService profileService;
    private final Tracer tracer;

    public PortfolioContentController(ProfileService profileService, Tracer tracer) {
        this.profileService = profileService;
        this.tracer = tracer;
    }

    @PostMapping("/projects")
    @PreAuthorize("hasAnyRole('USER', 'PRO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Project>> addProject(Authentication auth, @RequestBody Project project) {
        Project saved = profileService.addProject(auth.getName(), project);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project added successfully", saved, getTraceId()));
    }

    @PostMapping("/skills")
    @PreAuthorize("hasAnyRole('USER', 'PRO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Skill>> addSkill(Authentication auth, @RequestBody Skill skill) {
        Skill saved = profileService.addSkill(auth.getName(), skill);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Skill added successfully", saved, getTraceId()));
    }

    @PostMapping("/education")
    @PreAuthorize("hasAnyRole('USER', 'PRO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Education>> addEducation(Authentication auth, @RequestBody Education education) {
        Education saved = profileService.addEducation(auth.getName(), education);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Education added successfully", saved, getTraceId()));
    }

    @PostMapping("/experience")
    @PreAuthorize("hasAnyRole('USER', 'PRO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Experience>> addExperience(Authentication auth, @RequestBody Experience experience) {
        Experience saved = profileService.addExperience(auth.getName(), experience);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Experience added successfully", saved, getTraceId()));
    }

    @PostMapping("/certifications")
    @PreAuthorize("hasAnyRole('USER', 'PRO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Certification>> addCertification(Authentication auth, @RequestBody Certification cert) {
        Certification saved = profileService.addCertification(auth.getName(), cert);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Certification added successfully", saved, getTraceId()));
    }

    /**
     * Helper to extract current trace ID for unified observability across microservices.
     */
    private String getTraceId() {
        return (tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : "N/A";
    }
}
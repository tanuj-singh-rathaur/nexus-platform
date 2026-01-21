package com.rathaur.nexus.portfolioservice.controller;

import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.portfolioservice.entity.Profile;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio/profiles")
@Observed(name = "portfolio.profile.controller")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileService profileService;
    private final Tracer tracer;

    public ProfileController(ProfileService profileService, Tracer tracer) {
        this.profileService = profileService;
        this.tracer = tracer;
    }

    /**
     * PUBLIC ENDPOINT: Anyone can view a profile by username.
     * We keep this because profiles are meant to be shared.
     */
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<Profile>> getPublicProfile(@PathVariable String username) {
        Profile profile = profileService.getProfileByUsername(username);
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched", profile, getTraceId()));
    }

    /**
     * SECURE ENDPOINT: Fetch the profile of the logged-in user.
     * No username in URL.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_PRO', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Profile>> getMyProfile(Authentication auth) {
        if (auth == null) {
            log.error("SECURITY-CHECK: Authentication is NULL! (The JWT Filter likely didn't run)");
        } else {
            log.info("SECURITY-CHECK: User: {} | Authorities: {} | Authenticated: {}",
                    auth.getName(), auth.getAuthorities(), auth.isAuthenticated());
        }
        Profile profile = profileService.getProfileByUsername(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("My profile fetched", profile, getTraceId()));
    }

    /**
     * SECURE ENDPOINT: Update the profile of the logged-in user.
     */
    @PatchMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_PRO', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Profile>> updateMyProfile(Authentication auth, @RequestBody Profile profile) {
        // auth.getName() extracts the 'sub' (username) from the verified JWT
        Profile updated = profileService.updateProfile(auth.getName(), profile);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated, getTraceId()));
    }

    private String getTraceId() {
        return (tracer.currentSpan() != null) ? tracer.currentSpan().context().traceId() : "N/A";
    }
}
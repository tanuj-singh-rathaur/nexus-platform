package com.rathaur.nexus.portfolioservice.controller;

import com.rathaur.nexus.portfolioservice.common.ApiResponse;
import com.rathaur.nexus.portfolioservice.entity.Profile;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import io.micrometer.observation.annotation.Observed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio/profiles")
@Observed(name = "portfolio.controller")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Profile>> createProfile(@RequestBody Profile profile) {
        Profile createdProfile = profileService.createProfile(profile);
        return new ResponseEntity<>(new ApiResponse<>("Profile created successfully", createdProfile), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Profile>> getProfile(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        return ResponseEntity.ok(new ApiResponse<>("Profile fetched successfully", profile));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<Profile>> getProfileByUsername(@PathVariable String username) {
        Profile profile = profileService.getProfileByUsername(username);
        return ResponseEntity.ok(new ApiResponse<>("Profile fetched successfully", profile));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Profile>> updateProfile(@PathVariable Long id, @RequestBody Profile profile) {
        Profile updatedProfile = profileService.updateProfile(id, profile);
        return ResponseEntity.ok(new ApiResponse<>("Profile updated successfully", updatedProfile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok(new ApiResponse<>("Profile deleted successfully", true));
    }
}
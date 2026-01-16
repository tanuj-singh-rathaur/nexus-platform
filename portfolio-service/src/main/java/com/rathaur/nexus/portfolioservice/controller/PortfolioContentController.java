package com.rathaur.nexus.portfolioservice.controller;

import com.rathaur.nexus.portfolioservice.common.ApiResponse;
import com.rathaur.nexus.portfolioservice.entity.*;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio/profiles/{profileId}")
public class PortfolioContentController {

    private final ProfileService profileService;

    public PortfolioContentController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // --- PROJECTS ---
    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<Project>> addProject(@PathVariable Long profileId, @RequestBody Project project) {
        Project savedProject = profileService.addProjectToProfile(profileId, project);
        return new ResponseEntity<>(new ApiResponse<>("Project added successfully", savedProject), HttpStatus.CREATED);
    }

    // --- SKILLS ---
    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Skill>> addSkill(@PathVariable Long profileId, @RequestBody Skill skill) {
        Skill savedSkill = profileService.addSkillToProfile(profileId, skill);
        return new ResponseEntity<>(new ApiResponse<>("Skill added successfully", savedSkill), HttpStatus.CREATED);
    }

    // --- EDUCATION ---
    @PostMapping("/education")
    public ResponseEntity<ApiResponse<Education>> addEducation(@PathVariable Long profileId, @RequestBody Education education) {
        Education savedEducation = profileService.addEducationToProfile(profileId, education);
        return new ResponseEntity<>(new ApiResponse<>("Education added successfully", savedEducation), HttpStatus.CREATED);
    }

    // --- EXPERIENCE ---
    @PostMapping("/experience")
    public ResponseEntity<ApiResponse<Experience>> addExperience(@PathVariable Long profileId, @RequestBody Experience experience) {
        Experience savedExperience = profileService.addExperienceToProfile(profileId, experience);
        return new ResponseEntity<>(new ApiResponse<>("Experience added successfully", savedExperience), HttpStatus.CREATED);
    }

    // --- CERTIFICATIONS ---
    @PostMapping("/certifications")
    public ResponseEntity<ApiResponse<Certification>> addCertification(@PathVariable Long profileId, @RequestBody Certification certification) {
        Certification savedCertification = profileService.addCertificationToProfile(profileId, certification);
        return new ResponseEntity<>(new ApiResponse<>("Certification added successfully", savedCertification), HttpStatus.CREATED);
    }
}
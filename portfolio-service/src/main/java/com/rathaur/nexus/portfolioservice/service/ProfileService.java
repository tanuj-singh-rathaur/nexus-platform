package com.rathaur.nexus.portfolioservice.service;

import com.rathaur.nexus.portfolioservice.entity.*;
import com.rathaur.nexus.portfolioservice.exception.PortfolioDomainExceptions.*;
import com.rathaur.nexus.portfolioservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Service orchestrating Portfolio Profile operations.
 * Implements strict ownership checks to prevent IDOR vulnerabilities.
 * * @author Tanuj Singh Rathaur
 * @date 01/26/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificationRepository certificationRepository;

    // --- PROFILE CORE LOGIC ---

    public Profile createProfile(Profile profile) {
        String username = profile.getUsername().toLowerCase().trim();

        if ("fail_test".equals(username)) {
            throw new PortfolioDataException("Simulated Saga Failure for user: " + username);
        }

        return profileRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.info("NEXUS-PORTFOLIO: Creating new profile for: {}", username);
                    profile.setUsername(username);
                    initializeCollections(profile);
                    return profileRepository.save(profile);
                });
    }

    public Profile getProfileByUsername(String username) {
        return profileRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for: " + username));
    }

    public Profile updateProfile(String username, Profile incoming) {
        Profile existing = getProfileByUsername(username);

        if (incoming.getFullName() != null) existing.setFullName(incoming.getFullName());
        if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());
        if (incoming.getTitle() != null) existing.setTitle(incoming.getTitle());
        if (incoming.getAboutMe() != null) existing.setAboutMe(incoming.getAboutMe());

        return profileRepository.save(existing);
    }

    // --- PROJECT MANAGEMENT (With Ownership Logic) ---

    public Project addProject(String username, Project project) {
        Profile profile = getProfileByUsername(username);
        project.setProfile(profile);
        return projectRepository.save(project);
    }

    public void deleteProject(String username, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProfileNotFoundException("Project not found with ID: " + projectId));

        // SECURITY CHECK: Verify that the project belongs to the user in the JWT
        validateOwnership(username, project.getProfile().getUsername());

        projectRepository.delete(project);
        log.info("NEXUS-PORTFOLIO: Project {} deleted by user {}", projectId, username);
    }

    // --- SKILL MANAGEMENT ---

    public Skill addSkill(String username, Skill skill) {
        Profile profile = getProfileByUsername(username);

        boolean duplicate = profile.getSkills().stream()
                .anyMatch(s -> s.getSkillName().equalsIgnoreCase(skill.getSkillName()));

        if (duplicate) {
            throw new PortfolioDataException("Skill '" + skill.getSkillName() + "' already exists.");
        }

        skill.setProfile(profile);
        return skillRepository.save(skill);
    }

    public void removeSkill(String username, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ProfileNotFoundException("Skill not found with ID: " + skillId));

        validateOwnership(username, skill.getProfile().getUsername());
        skillRepository.delete(skill);
    }

    // --- EDUCATION, EXPERIENCE, & CERTS (Standard Flow) ---

    public Education addEducation(String username, Education education) {
        education.setProfile(getProfileByUsername(username));
        return educationRepository.save(education);
    }

    public Experience addExperience(String username, Experience experience) {
        experience.setProfile(getProfileByUsername(username));
        return experienceRepository.save(experience);
    }

    public Certification addCertification(String username, Certification cert) {
        cert.setProfile(getProfileByUsername(username));
        return certificationRepository.save(cert);
    }

    // --- INTERNAL HELPERS ---

    /**
     * The Security Shield: Ensures that 'requester' is the 'owner' of the target resource.
     */
    private void validateOwnership(String requester, String owner) {
        if (!requester.equalsIgnoreCase(owner)) {
            log.warn("SECURITY-BREACH: User {} attempted to modify resource belonging to {}", requester, owner);
            throw new ResourceOwnershipException("Access Denied: You do not own this resource.");
        }
    }

    private void initializeCollections(Profile profile) {
        profile.setProjects(new ArrayList<>());
        profile.setEducation(new ArrayList<>());
        profile.setSkills(new ArrayList<>());
        profile.setExperienceList(new ArrayList<>());
        profile.setCertifications(new ArrayList<>());
    }
}
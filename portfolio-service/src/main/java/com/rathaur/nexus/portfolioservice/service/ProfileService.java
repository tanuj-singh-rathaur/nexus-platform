package com.rathaur.nexus.portfolioservice.service;

import com.rathaur.nexus.portfolioservice.entity.*;
import com.rathaur.nexus.portfolioservice.exception.ProfileNotFoundException;
import com.rathaur.nexus.portfolioservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Service orchestrating Portfolio Profile operations.
 * Designed for stateless operation where identity is derived from JWT.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Service
@Transactional
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificationRepository certificationRepository;

    public ProfileService(ProfileRepository profileRepository,
                          ProjectRepository projectRepository,
                          SkillRepository skillRepository,
                          EducationRepository educationRepository,
                          ExperienceRepository experienceRepository,
                          CertificationRepository certificationRepository) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.certificationRepository = certificationRepository;
    }

    /**
     * Idempotent profile creation. Used by RabbitMQ listeners.
     */
    public Profile createProfile(Profile profile) {
        String username = profile.getUsername().toLowerCase().trim();

        // TEMPORARY: Force a Saga Failure for testing
        if (profile.getUsername().equals("fail_test")) {
            throw new RuntimeException("Simulated Database Failure for Saga Testing");
        }

        return profileRepository.findByUsername(username)
                .map(existing -> {
                    log.info("SaaS-INFO: Profile already exists for user: {}", username);
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("SaaS-INFO: Initializing new profile for user: {}", username);
                    profile.setUsername(username);
                    initializeCollections(profile);
                    return profileRepository.save(profile);
                });
    }

    public Profile getProfileByUsername(String username) {
        return profileRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for username: " + username));
    }

    /**
     * Updates profile data.
     * Guarded to only update allowed fields.
     */
    public Profile updateProfile(String username, Profile incoming) {
        Profile existing = getProfileByUsername(username);

        // Manual mapping protects immutable fields like 'id' and 'username'
        if (incoming.getFullName() != null) existing.setFullName(incoming.getFullName());
        if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());
        if (incoming.getTitle() != null) existing.setTitle(incoming.getTitle());
        if (incoming.getAboutMe() != null) existing.setAboutMe(incoming.getAboutMe());

        return profileRepository.save(existing);
    }

    public void deleteProfile(String username) {
        Profile profile = getProfileByUsername(username);
        profileRepository.delete(profile);
    }

    /* --- Child Entity Operations --- */

    public Project addProject(String username, Project project) {
        Profile profile = getProfileByUsername(username);
        project.setProfile(profile); // Enforce Relationship
        return projectRepository.save(project);
    }

    public Skill addSkill(String username, Skill skill) {
        Profile profile = getProfileByUsername(username);
        skill.setProfile(profile);
        return skillRepository.save(skill);
    }

    public Education addEducation(String username, Education education) {
        Profile profile = getProfileByUsername(username);
        education.setProfile(profile);
        return educationRepository.save(education);
    }

    public Experience addExperience(String username, Experience experience) {
        Profile profile = getProfileByUsername(username);
        experience.setProfile(profile);
        return experienceRepository.save(experience);
    }

    public Certification addCertification(String username, Certification certification) {
        Profile profile = getProfileByUsername(username);
        certification.setProfile(profile);
        return certificationRepository.save(certification);
    }

    /* Helper to ensure no null pointer exceptions on new profiles */
    private void initializeCollections(Profile profile) {
        profile.setProjects(new ArrayList<>());
        profile.setEducation(new ArrayList<>());
        profile.setSkills(new ArrayList<>());
        profile.setExperienceList(new ArrayList<>());
        profile.setCertifications(new ArrayList<>());
    }
}
package com.rathaur.nexus.portfolioservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use Spring's Transactional for better integration

import com.rathaur.nexus.portfolioservice.entity.*;
import com.rathaur.nexus.portfolioservice.repository.*;

@Service
@Transactional
public class ProfileService {

    // 1. Declare all Repositories
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificationRepository certificationRepository;

    // 2. Constructor Injection for all Repositories
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

    // --- PROFILE OPERATIONS ---

    public Profile createProfile(Profile profile) {
        // Optional: Check if username exists before saving
        // 1. Validation Logic
        if (profile.getUsername() != null &&
                profileRepository.findByUsername(profile.getUsername()).isPresent()) {
            throw new RuntimeException("Profile for this username already exists!");
        }

        // 2. Initialize Collections (Ensures JSON response shows [] instead of null)
        if (profile.getProjects() == null) profile.setProjects(new ArrayList<>());
        if (profile.getEducation() == null) profile.setEducation(new ArrayList<>());
        if (profile.getSkills() == null) profile.setSkills(new ArrayList<>());
        if (profile.getExperienceList() == null) profile.setExperienceList(new ArrayList<>());
        if (profile.getCertifications() == null) profile.setCertifications(new ArrayList<>());

        return profileRepository.save(profile);
    }

    public List<Profile> getAllProfile() {
        return profileRepository.findAll();
    }

    public Profile getProfileById(Long profileId){
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile Not Found with ID: " + profileId));
    }

    public Profile getProfileByUsername(String username) {
        return profileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Profile Not Found with username: " + username));
    }

    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Profile Not Found with ID: " + id);
        }
        profileRepository.deleteById(id);
    }

    public Profile updateProfile(Long id, Profile incomingProfile) {
        Profile existingProfile = getProfileById(id);

        // Update fields only if they are not null (Patch logic)
        if (incomingProfile.getFullName() != null) existingProfile.setFullName(incomingProfile.getFullName());
        if (incomingProfile.getTitle() != null) existingProfile.setTitle(incomingProfile.getTitle());
        if (incomingProfile.getAboutMe() != null) existingProfile.setAboutMe(incomingProfile.getAboutMe());
        if (incomingProfile.getTheme() != null) existingProfile.setTheme(incomingProfile.getTheme());
        if (incomingProfile.getEmail() != null) existingProfile.setEmail(incomingProfile.getEmail());
        if (incomingProfile.getPhone() != null) existingProfile.setPhone(incomingProfile.getPhone());

        // Social Links
        if (incomingProfile.getLinkedinUrl() != null) existingProfile.setLinkedinUrl(incomingProfile.getLinkedinUrl());
        if (incomingProfile.getGithubUrl() != null) existingProfile.setGithubUrl(incomingProfile.getGithubUrl());
        if (incomingProfile.getTwitterUrl() != null) existingProfile.setTwitterUrl(incomingProfile.getTwitterUrl());
        if (incomingProfile.getWebsiteUrl() != null) existingProfile.setWebsiteUrl(incomingProfile.getWebsiteUrl());

        return profileRepository.save(existingProfile);
    }

    // --- CHILD ENTITY OPERATIONS (Add Items) ---

    public Project addProjectToProfile(Long profileId, Project project){
        Profile profile = getProfileById(profileId); // Reusing the helper method
        project.setProfile(profile);
        return projectRepository.save(project);
    }

    public Skill addSkillToProfile(Long profileId, Skill skill) {
        Profile profile = getProfileById(profileId);
        skill.setProfile(profile);
        return skillRepository.save(skill);
    }

    public Education addEducationToProfile(Long profileId, Education education) {
        Profile profile = getProfileById(profileId);
        education.setProfile(profile);
        return educationRepository.save(education);
    }

    public Experience addExperienceToProfile(Long profileId, Experience experience) {
        Profile profile = getProfileById(profileId);
        experience.setProfile(profile);
        return experienceRepository.save(experience);
    }

    public Certification addCertificationToProfile(Long profileId, Certification certification) {
        Profile profile = getProfileById(profileId);
        certification.setProfile(profile);
        return certificationRepository.save(certification);
    }
}
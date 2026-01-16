package com.rathaur.nexus.portfolioservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Profile extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    private String theme;

    private String fullName;
    private String title;
    private String email;
    private String phone;

    @Column(length = 5000)
    private String aboutMe;

    //social links
    private String linkedinUrl;
    private String githubUrl;
    private String twitterUrl;
    private String websiteUrl;

    //Relationship

    //oneToMany
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Skill> skills;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Experience> experienceList;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Education> education;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Project> projects;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Certification> certifications;

}

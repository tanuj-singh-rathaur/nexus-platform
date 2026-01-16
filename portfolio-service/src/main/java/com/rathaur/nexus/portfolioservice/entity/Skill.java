package com.rathaur.nexus.portfolioservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Skill extends BaseEntity{

    private String skillName;
    private String category;
    private Integer rating;
    private Integer yearsOfExperience;

    @ManyToOne
    @JoinColumn(name="profile_id")
    @JsonIgnore
    private Profile profile;
}

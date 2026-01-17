package com.rathaur.nexus.portfolioservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Experience extends BaseEntity{

    private String companyName;
    private String role;
    private String location;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrentRole;

    @Column(length = 2000)
    private String roleDescription;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private Profile profile;

}

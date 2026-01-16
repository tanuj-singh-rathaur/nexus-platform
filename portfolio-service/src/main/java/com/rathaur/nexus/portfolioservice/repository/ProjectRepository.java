package com.rathaur.nexus.portfolioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rathaur.nexus.portfolioservice.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long>{
        List<Project> findByProfileId(long profileId);
        List<Project> findByProfileIdAndIsFeaturedTrue(long profileId);
}

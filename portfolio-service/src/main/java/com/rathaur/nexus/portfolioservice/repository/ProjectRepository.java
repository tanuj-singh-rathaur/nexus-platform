package com.rathaur.nexus.portfolioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rathaur.nexus.portfolioservice.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long>{
        List<Project> findByUserId(String userId);
}

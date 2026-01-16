package com.rathaur.nexus.portfolioservice.repository;

import com.rathaur.nexus.portfolioservice.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByProfileId(Long profileId);
    List<Experience> findByProfileIdOrderByStartDateDesc(Long profileId);
}

package com.rathaur.nexus.portfolioservice.repository;

import com.rathaur.nexus.portfolioservice.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByProfileId(Long profileId);
    List<Education> findByProfileIdOrderByEndDateDesc(Long profileID);
}

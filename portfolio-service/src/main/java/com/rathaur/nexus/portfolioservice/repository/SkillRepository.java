package com.rathaur.nexus.portfolioservice.repository;

import com.rathaur.nexus.portfolioservice.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByProfileId(Long profileId);
    List<Skill> findByProfileIdAndCategory(Long profileId, String category);

}

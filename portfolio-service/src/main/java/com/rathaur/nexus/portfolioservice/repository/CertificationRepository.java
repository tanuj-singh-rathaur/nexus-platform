package com.rathaur.nexus.portfolioservice.repository;

import com.rathaur.nexus.portfolioservice.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByProfileId(Long profileId);
}

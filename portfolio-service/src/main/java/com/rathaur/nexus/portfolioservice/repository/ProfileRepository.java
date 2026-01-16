package com.rathaur.nexus.portfolioservice.repository;

import com.rathaur.nexus.portfolioservice.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
        Optional<Profile> findByUsername(String username);
        Optional<Profile> findByEmail(String email);
}


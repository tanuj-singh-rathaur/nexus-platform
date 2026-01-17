package com.rathaur.nexus.identityservice.repository;

import com.rathaur.nexus.identityservice.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
     Optional<UserCredential> findByName(String username);
}

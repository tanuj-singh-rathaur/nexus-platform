package com.rathaur.nexus.identityservice.repository;

import com.rathaur.nexus.identityservice.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
     // Find by the unique username field
     Optional<UserCredential> findByUsername(String username);

     // Find by the unique email field
     Optional<UserCredential> findByEmail(String email);

     // Senior Developer Tip: Allow login with EITHER username OR email
     Optional<UserCredential> findByUsernameOrEmail(String username, String email);
}

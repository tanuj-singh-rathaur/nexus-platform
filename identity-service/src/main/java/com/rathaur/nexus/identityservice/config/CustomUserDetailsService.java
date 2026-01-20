package com.rathaur.nexus.identityservice.config;

import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialRepository userCredentialRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserCredential> credential = userCredentialRepository.findByUsername(username);
        return credential.map(CustomUserDetails::new)
                .orElseThrow(()->new UsernameNotFoundException("User Not Found!"));
    }
}

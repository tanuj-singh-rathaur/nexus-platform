package com.rathaur.nexus.identityservice.config;

import com.rathaur.nexus.identityservice.entity.UserCredential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final boolean isEnabled;
    private final boolean isLocked;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(UserCredential userCredential) {
        // Fix: Use unique username for login, not display name
        this.username = userCredential.getUsername();
        this.password = userCredential.getPassword();
        this.isEnabled = userCredential.isEnabled();
        this.isLocked = userCredential.isLocked();

        // Map Role Enum to Spring Authority
        this.authorities = List.of(new SimpleGrantedAuthority(userCredential.getRole().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked; // User is unlocked if isLocked is false
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
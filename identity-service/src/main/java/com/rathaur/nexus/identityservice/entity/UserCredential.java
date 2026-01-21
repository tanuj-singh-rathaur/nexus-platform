package com.rathaur.nexus.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Better for MySQL/Postgres
    private Integer id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Username cannot be empty")
    @Column(unique = true) // Ensures handles like @rathaur are unique
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    private boolean isEnabled = true;
    private boolean isLocked = false;
}



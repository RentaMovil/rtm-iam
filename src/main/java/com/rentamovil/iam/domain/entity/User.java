package com.rentamovil.iam.domain.entity;

import java.time.LocalDateTime;

import com.rentamovil.iam.domain.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Integer id;
    private Person person;

    // Un usuario tiene exactamente un rol, siempre (user.role_id es FK directa)
    private Role role;

    private String username;
    private String passwordHash;
    private UserStatus status;
    private LocalDateTime lastLogin;

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}

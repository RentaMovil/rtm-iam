package com.rentamovil.iam.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Quién está haciendo la petición, sacado del access token (claims sub, role, permissions)
@Getter
@AllArgsConstructor
public class AuthenticatedUser {

    private Integer userId;
    private String role;
    private List<String> permissions;

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}

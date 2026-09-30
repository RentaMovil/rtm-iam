package com.rentamovil.iam.infrastructure.web.dto;

import com.rentamovil.iam.domain.entity.User;

import lombok.Builder;
import lombok.Getter;

// UserSummary de iam.yaml. Nunca incluye el hash de la contraseña.
@Getter
@Builder
public class UserResponse {

    private Integer id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String status;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getPerson().getEmail())
                .username(user.getUsername())
                .firstName(user.getPerson().getFirstName())
                .lastName(user.getPerson().getLastName())
                .role(user.getRole().getName())
                .status(user.getStatus().name())
                .build();
    }
}

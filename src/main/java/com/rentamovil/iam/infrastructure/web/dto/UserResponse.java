package com.rentamovil.iam.infrastructure.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rentamovil.iam.domain.entity.User;

import lombok.Builder;
import lombok.Getter;

// UserSummary de iam.yaml. Nunca incluye el hash de la contraseña.
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponse {

    private Integer id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String role;
    private String status;
    private List<String> permissions;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getPerson().getEmail())
                .username(user.getUsername())
                .firstName(user.getPerson().getFirstName())
                .lastName(user.getPerson().getLastName())
                .imageUrl(user.getPerson().getImageUrl())
                .role(user.getRole().getName())
                .status(user.getStatus().name())
                .permissions(user.getRole().getPermissions())
                .build();
    }
}

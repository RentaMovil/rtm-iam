package com.rentamovil.iam.infrastructure.web.dto;

import com.rentamovil.iam.domain.entity.User;

import lombok.Builder;
import lombok.Getter;

// UserProfile de iam.yaml: los datos del usuario más el teléfono y la foto
@Getter
@Builder
public class ProfileResponse {

    private Integer id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String imageUrl;
    private String role;
    private String status;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getPerson().getEmail())
                .username(user.getUsername())
                .firstName(user.getPerson().getFirstName())
                .lastName(user.getPerson().getLastName())
                .phone(user.getPerson().getPhone())
                .imageUrl(user.getPerson().getImageUrl())
                .role(user.getRole().getName())
                .status(user.getStatus().name())
                .build();
    }
}

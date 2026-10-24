package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /users/me/push-token
@Getter
@Setter
public class RegisterPushTokenRequest {

    @NotBlank
    @Size(max = 255)
    private String token;

    @NotBlank
    @Pattern(regexp = "ios|android", message = "Debe ser 'ios' o 'android'")
    private String platform;
}

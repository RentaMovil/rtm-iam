package com.rentamovil.iam.domain.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Token de Expo Push de un dispositivo del usuario (iam.device_token). Un usuario puede tener
// varios (uno por dispositivo en el que inició sesión).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushDeviceToken {

    private Integer id;
    private Integer userId;
    private String token;
    private String platform;
    private LocalDateTime createdAt;
}

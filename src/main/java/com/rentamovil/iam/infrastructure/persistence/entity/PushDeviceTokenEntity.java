package com.rentamovil.iam.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device_token")
@Getter
@Setter
@NoArgsConstructor
public class PushDeviceTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "platform", nullable = false, length = 20)
    private String platform;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime")
    private LocalDateTime createdAt;
}

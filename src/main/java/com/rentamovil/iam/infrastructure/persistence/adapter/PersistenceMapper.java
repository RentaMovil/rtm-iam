package com.rentamovil.iam.infrastructure.persistence.adapter;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.PushDeviceToken;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.infrastructure.persistence.entity.PersonEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.PushDeviceTokenEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.RoleEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.SessionEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.UserEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.VerificationCodeEntity;

// Convierte entre las entidades del dominio y las entidades JPA (tablas)
public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static Person toDomain(PersonEntity entity) {
        return Person.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    public static PersonEntity toEntity(Person person) {
        PersonEntity entity = new PersonEntity();
        entity.setId(person.getId());
        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setEmail(person.getEmail());
        entity.setPhone(person.getPhone());
        entity.setImageUrl(person.getImageUrl());
        return entity;
    }

    public static Role toDomain(RoleEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public static RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        return entity;
    }

    public static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .person(toDomain(entity.getPerson()))
                .role(toDomain(entity.getRole()))
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .status(UserStatus.valueOf(entity.getStatus()))
                .lastLogin(entity.getLastLogin())
                .build();
    }

    public static Session toDomain(SessionEntity entity) {
        return Session.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .refreshTokenHash(entity.getRefreshToken())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .revoked(entity.isRevoked())
                .build();
    }

    public static SessionEntity toEntity(Session session) {
        SessionEntity entity = new SessionEntity();
        entity.setId(session.getId());
        entity.setUserId(session.getUserId());
        entity.setRefreshToken(session.getRefreshTokenHash());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setExpiresAt(session.getExpiresAt());
        entity.setIpAddress(session.getIpAddress());
        entity.setUserAgent(session.getUserAgent());
        entity.setRevoked(session.isRevoked());
        return entity;
    }

    public static VerificationCode toDomain(VerificationCodeEntity entity) {
        return VerificationCode.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .code(entity.getCode())
                .type(CodeType.valueOf(entity.getType()))
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .used(entity.isUsed())
                .build();
    }

    public static VerificationCodeEntity toEntity(VerificationCode verificationCode) {
        VerificationCodeEntity entity = new VerificationCodeEntity();
        entity.setId(verificationCode.getId());
        entity.setUserId(verificationCode.getUserId());
        entity.setCode(verificationCode.getCode());
        entity.setType(verificationCode.getType().name());
        entity.setCreatedAt(verificationCode.getCreatedAt());
        entity.setExpiresAt(verificationCode.getExpiresAt());
        entity.setUsed(verificationCode.isUsed());
        return entity;
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setPerson(toEntity(user.getPerson()));
        entity.setRole(toEntity(user.getRole()));
        entity.setUsername(user.getUsername());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setStatus(user.getStatus().name());
        entity.setLastLogin(user.getLastLogin());
        return entity;
    }

    public static PushDeviceToken toDomain(PushDeviceTokenEntity entity) {
        return PushDeviceToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .token(entity.getToken())
                .platform(entity.getPlatform())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

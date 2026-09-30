package com.rentamovil.iam.infrastructure.persistence.adapter;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.infrastructure.persistence.entity.PersonEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.RoleEntity;
import com.rentamovil.iam.infrastructure.persistence.entity.UserEntity;

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
                .build();
    }

    public static PersonEntity toEntity(Person person) {
        PersonEntity entity = new PersonEntity();
        entity.setId(person.getId());
        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setEmail(person.getEmail());
        entity.setPhone(person.getPhone());
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
}

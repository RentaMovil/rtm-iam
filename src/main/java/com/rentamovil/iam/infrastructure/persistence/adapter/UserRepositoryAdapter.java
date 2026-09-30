package com.rentamovil.iam.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.infrastructure.persistence.repository.UserJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return PersistenceMapper.toDomain(userJpaRepository.save(PersistenceMapper.toEntity(user)));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByPersonEmail(email).map(PersistenceMapper::toDomain);
    }
}

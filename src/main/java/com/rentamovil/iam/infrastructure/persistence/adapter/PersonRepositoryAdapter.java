package com.rentamovil.iam.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.infrastructure.persistence.repository.PersonJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersonRepositoryAdapter implements PersonRepositoryPort {

    private final PersonJpaRepository personJpaRepository;

    @Override
    public Person save(Person person) {
        return PersistenceMapper.toDomain(personJpaRepository.save(PersistenceMapper.toEntity(person)));
    }

    @Override
    public boolean existsByEmail(String email) {
        return personJpaRepository.existsByEmail(email);
    }
}

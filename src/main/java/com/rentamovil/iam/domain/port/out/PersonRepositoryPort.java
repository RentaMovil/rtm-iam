package com.rentamovil.iam.domain.port.out;

import com.rentamovil.iam.domain.entity.Person;

// Lo que el dominio necesita para guardar y consultar personas.
// La implementación real (JPA / SQL Server) vive en infrastructure/persistence.
public interface PersonRepositoryPort {

    Person save(Person person);

    boolean existsByEmail(String email);
}

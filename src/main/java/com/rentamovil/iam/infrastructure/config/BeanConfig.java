package com.rentamovil.iam.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.iam.application.usecase.RegisterUserUseCaseImpl;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

// Aquí se conectan los casos de uso con sus adaptadores.
// Los casos de uso no llevan @Service para que el dominio no dependa de Spring.
@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(PersonRepositoryPort personRepositoryPort,
                                                   UserRepositoryPort userRepositoryPort,
                                                   RoleRepositoryPort roleRepositoryPort,
                                                   PasswordHasherPort passwordHasherPort) {
        return new RegisterUserUseCaseImpl(personRepositoryPort, userRepositoryPort, roleRepositoryPort, passwordHasherPort);
    }
}

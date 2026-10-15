package com.rentamovil.iam.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    // Person y User se guardan juntos: si falla el user, no queda una person suelta
    @Override
    @Transactional
    public User register(Person person, String username, String rawPassword) {

        //El email se guarda en minúsculas para que "Ana@Mail.com" y "ana@mail.com" sean el mismo
        person.setEmail(person.getEmail().trim().toLowerCase());

        //Verificar que el email no esté registrado
        if (personRepositoryPort.existsByEmail(person.getEmail())) {
            throw new ResourceAlreadyExistsException("EMAIL_ALREADY_EXISTS", "El email ya está registrado");
        }

        //Verificar que el username no esté en uso
        if (userRepositoryPort.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("USERNAME_ALREADY_EXISTS", "El username ya está en uso");
        }

        //El registro público siempre crea un CLIENT, nunca un admin
        Role clientRole = roleRepositoryPort.findByName(Role.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el rol CLIENT, revisa rtm-iam-db"));

        Person savedPerson = personRepositoryPort.save(person);

        User newUser = User.builder()
                .person(savedPerson)
                .role(clientRole)
                .username(username)
                .passwordHash(passwordHasherPort.hash(rawPassword))
                .status(UserStatus.ACTIVE)
                .build();

        return userRepositoryPort.save(newUser);
    }
}

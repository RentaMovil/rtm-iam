package com.rentamovil.iam.application.usecase;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.IncorrectPasswordException;
import com.rentamovil.iam.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.ChangeMyEmailUseCase;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeMyEmailUseCaseImpl implements ChangeMyEmailUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    public User changeEmail(Integer userId, String newEmail, String currentPassword) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + userId));

        //Volver a pedir la contraseña confirma que quien cambia el email es el dueño de la cuenta
        if (!passwordHasherPort.matches(currentPassword, user.getPasswordHash())) {
            throw new IncorrectPasswordException();
        }

        String email = newEmail.trim().toLowerCase();

        //Si es el mismo email no hay nada que cambiar
        if (email.equals(user.getPerson().getEmail())) {
            return user;
        }

        //El email es único: no puede ser el de otra persona
        if (personRepositoryPort.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("EMAIL_ALREADY_EXISTS", "El email ya está registrado");
        }

        user.getPerson().setEmail(email);
        user.setPerson(personRepositoryPort.save(user.getPerson()));
        return user;
    }
}

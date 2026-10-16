package com.rentamovil.iam.application.usecase;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.domain.port.out.EmailSenderPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    private final EmailSenderPort emailSenderPort;
    private final long codeExpirySeconds;
    private final SecureRandom secureRandom = new SecureRandom();

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

        // Queda PENDING_VERIFICATION hasta que confirme el código enviado a su correo (HU-IAM-001):
        // no puede hacer login todavía, así que acá no se emite ningún token.
        User newUser = User.builder()
                .person(savedPerson)
                .role(clientRole)
                .username(username)
                .passwordHash(passwordHasherPort.hash(rawPassword))
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        User savedUser = userRepositoryPort.save(newUser);

        LocalDateTime now = LocalDateTime.now();
        VerificationCode verificationCode = VerificationCode.builder()
                .userId(savedUser.getId())
                .code(generateSixDigitCode())
                .type(CodeType.EMAIL_VERIFICATION)
                .createdAt(now)
                .expiresAt(now.plusSeconds(codeExpirySeconds))
                .used(false)
                .build();
        verificationCodeRepositoryPort.save(verificationCode);

        emailSenderPort.sendEmailVerificationCode(savedPerson.getEmail(), savedPerson.getFirstName(),
                verificationCode.getCode());

        return savedUser;
    }

    // 000000 a 999999, con SecureRandom (Random normal se puede predecir)
    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}

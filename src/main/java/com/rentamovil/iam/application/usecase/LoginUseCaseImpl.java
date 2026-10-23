package com.rentamovil.iam.application.usecase;

import java.time.LocalDateTime;
import java.util.Optional;

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.AccountBlockedException;
import com.rentamovil.iam.domain.exception.EmailNotVerifiedException;
import com.rentamovil.iam.domain.exception.InvalidCredentialsException;
import com.rentamovil.iam.domain.port.in.LoginUseCase;
import com.rentamovil.iam.domain.port.out.LoginAttemptPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

// Sin @Transactional a propósito: al 5.º intento fallido se guarda BLOCKED y después se lanza una
// excepción; con una transacción, esa excepción desharía el bloqueo.
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    public static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final LoginAttemptPort loginAttemptPort;
    private final AuthTokenService authTokenService;

    @Override
    public AuthTokens login(String identifier, String rawPassword, String ipAddress, String userAgent) {

        //Se puede entrar con email o con username; si no existe, no se dice cuál de los dos falló
        User user = findByEmailOrUsername(identifier.trim())
                .orElseThrow(InvalidCredentialsException::new);

        //Una cuenta bloqueada no entra ni con la contraseña correcta
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AccountBlockedException();
        }

        if (!passwordHasherPort.matches(rawPassword, user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new InvalidCredentialsException();
        }

        //Con la contraseña correcta ya sabemos que es dueño de la cuenta: avisarle que falta
        //verificar el correo no es filtrar nada que no supiera
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new EmailNotVerifiedException();
        }

        //Una cuenta desactivada por un SUPER_ADMIN tampoco puede entrar
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        loginAttemptPort.reset(user.getId());
        user.setLastLogin(LocalDateTime.now());
        userRepositoryPort.save(user);

        return authTokenService.issue(user, ipAddress, userAgent);
    }

    private Optional<User> findByEmailOrUsername(String identifier) {
        if (identifier.contains("@")) {
            return userRepositoryPort.findByEmail(identifier.toLowerCase());
        }
        return userRepositoryPort.findByUsername(identifier);
    }

    //Al 5.º fallo seguido la cuenta queda BLOCKED en la base (no se desbloquea sola)
    private void registerFailedAttempt(User user) {
        int failures = loginAttemptPort.registerFailure(user.getId());
        if (failures >= MAX_FAILED_ATTEMPTS) {
            user.setStatus(UserStatus.BLOCKED);
            userRepositoryPort.save(user);
            loginAttemptPort.reset(user.getId());
        }
    }
}

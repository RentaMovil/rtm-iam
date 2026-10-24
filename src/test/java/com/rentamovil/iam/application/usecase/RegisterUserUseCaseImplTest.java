package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.common.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.domain.port.out.EmailSenderPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    @Mock
    private PersonRepositoryPort personRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private RoleRepositoryPort roleRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    @Mock
    private EmailSenderPort emailSenderPort;

    // @InjectMocks no sirve acá: el último parámetro (codeExpirySeconds) es un long, no un mock.
    private RegisterUserUseCaseImpl registerUserUseCase;

    private Person person;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCaseImpl(personRepositoryPort, userRepositoryPort,
                roleRepositoryPort, passwordHasherPort, verificationCodeRepositoryPort, emailSenderPort, 900L);
        person = Person.builder().firstName("María").lastName("García").email("Maria.Garcia@Example.com").build();
    }

    @Test
    void registraUnClientePendienteDeVerificacionYMandaElCodigo() {
        when(personRepositoryPort.existsByEmail("maria.garcia@example.com")).thenReturn(false);
        when(userRepositoryPort.existsByUsername("mgarcia")).thenReturn(false);
        when(roleRepositoryPort.findByName(Role.CLIENT)).thenReturn(Optional.of(Role.builder().id(1).name(Role.CLIENT).build()));
        when(personRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordHasherPort.hash("SecurePass123!")).thenReturn("$2a$12$hash");
        when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User user = registerUserUseCase.register(person, "mgarcia", "SecurePass123!");

        assertThat(user.getRole().getName()).isEqualTo(Role.CLIENT);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.getPasswordHash()).isEqualTo("$2a$12$hash");
        assertThat(user.getPerson().getEmail()).isEqualTo("maria.garcia@example.com");

        verify(verificationCodeRepositoryPort).save(any());
        verify(emailSenderPort).sendEmailVerificationCode(eq("maria.garcia@example.com"), eq("María"), any());
    }

    @Test
    void rechazaUnEmailYaRegistrado() {
        when(personRepositoryPort.existsByEmail("maria.garcia@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUserUseCase.register(person, "mgarcia", "SecurePass123!"))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .extracting("code").isEqualTo("EMAIL_ALREADY_EXISTS");
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void rechazaUnUsernameEnUso() {
        when(personRepositoryPort.existsByEmail("maria.garcia@example.com")).thenReturn(false);
        when(userRepositoryPort.existsByUsername("mgarcia")).thenReturn(true);

        assertThatThrownBy(() -> registerUserUseCase.register(person, "mgarcia", "SecurePass123!"))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .extracting("code").isEqualTo("USERNAME_ALREADY_EXISTS");
        verify(personRepositoryPort, never()).save(any());
    }
}

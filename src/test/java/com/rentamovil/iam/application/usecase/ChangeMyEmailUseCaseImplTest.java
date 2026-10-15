package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.IncorrectPasswordException;
import com.rentamovil.iam.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ChangeMyEmailUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PersonRepositoryPort personRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private ChangeMyEmailUseCaseImpl changeMyEmailUseCase;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .person(Person.builder().id(1).email("maria@example.com").build())
                .passwordHash("$2a$12$hash")
                .build();
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));
    }

    @Test
    void cambiaElEmailSiLaContrasenaEsCorrecta() {
        when(passwordHasherPort.matches("SecurePass123!", "$2a$12$hash")).thenReturn(true);
        when(personRepositoryPort.existsByEmail("nuevo@example.com")).thenReturn(false);
        when(personRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = changeMyEmailUseCase.changeEmail(1, " Nuevo@Example.com ", "SecurePass123!");

        assertThat(result.getPerson().getEmail()).isEqualTo("nuevo@example.com");
    }

    @Test
    void contrasenaIncorrectaNoCambiaElEmail() {
        when(passwordHasherPort.matches("mala", "$2a$12$hash")).thenReturn(false);

        assertThatThrownBy(() -> changeMyEmailUseCase.changeEmail(1, "nuevo@example.com", "mala"))
                .isInstanceOf(IncorrectPasswordException.class);
        assertThat(user.getPerson().getEmail()).isEqualTo("maria@example.com");
        verify(personRepositoryPort, never()).save(any());
    }

    @Test
    void emailDeOtraPersonaDa409() {
        when(passwordHasherPort.matches("SecurePass123!", "$2a$12$hash")).thenReturn(true);
        when(personRepositoryPort.existsByEmail("otro@example.com")).thenReturn(true);

        assertThatThrownBy(() -> changeMyEmailUseCase.changeEmail(1, "otro@example.com", "SecurePass123!"))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(personRepositoryPort, never()).save(any());
    }
}

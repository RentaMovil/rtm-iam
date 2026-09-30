package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.AccountBlockedException;
import com.rentamovil.iam.domain.exception.InvalidCredentialsException;
import com.rentamovil.iam.domain.port.out.LoginAttemptPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PermissionRepositoryPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PermissionRepositoryPort permissionRepositoryPort;
    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private TokenProviderPort tokenProviderPort;
    @Mock
    private LoginAttemptPort loginAttemptPort;

    private LoginUseCaseImpl loginUseCase;
    private User user;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCaseImpl(userRepositoryPort, permissionRepositoryPort, sessionRepositoryPort,
                passwordHasherPort, tokenProviderPort, loginAttemptPort, 604800);

        user = User.builder()
                .id(1)
                .person(Person.builder().id(1).email("maria@example.com").build())
                .role(Role.builder().id(1).name(Role.CLIENT).build())
                .username("mgarcia")
                .passwordHash("$2a$12$hash")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void loginCorrectoDevuelveTokensYGuardaLaSesion() {
        when(userRepositoryPort.findByEmail("maria@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("SecurePass123!", "$2a$12$hash")).thenReturn(true);
        when(permissionRepositoryPort.findNamesByRoleId(1)).thenReturn(List.of("vehicles:read"));
        when(tokenProviderPort.generateAccessToken(user)).thenReturn("jwt");
        when(tokenProviderPort.generateRefreshToken()).thenReturn("refresh");
        when(tokenProviderPort.hashRefreshToken("refresh")).thenReturn("hash-refresh");
        when(tokenProviderPort.getAccessTokenExpiresIn()).thenReturn(1800L);

        AuthTokens tokens = loginUseCase.login("Maria@Example.com", "SecurePass123!", "127.0.0.1", "test");

        assertThat(tokens.getAccessToken()).isEqualTo("jwt");
        assertThat(tokens.getRefreshToken()).isEqualTo("refresh");
        assertThat(tokens.getExpiresIn()).isEqualTo(1800L);
        assertThat(user.getRole().getPermissions()).containsExactly("vehicles:read");
        assertThat(user.getLastLogin()).isNotNull();

        //En la base se guarda el hash del refresh token, no el token
        ArgumentCaptor<Session> session = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepositoryPort).save(session.capture());
        assertThat(session.getValue().getRefreshTokenHash()).isEqualTo("hash-refresh");
        verify(loginAttemptPort).reset(1);
    }

    @Test
    void usuarioInexistenteDa401() {
        when(userRepositoryPort.findByUsername("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login("noexiste", "SecurePass123!", null, null))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void contrasenaIncorrectaDa401YCuentaElIntento() {
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("mala", "$2a$12$hash")).thenReturn(false);
        when(loginAttemptPort.registerFailure(1)).thenReturn(1);

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "mala", null, null))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(sessionRepositoryPort, never()).save(any());
    }

    @Test
    void alQuintoFalloLaCuentaQuedaBloqueada() {
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("mala", "$2a$12$hash")).thenReturn(false);
        when(loginAttemptPort.registerFailure(1)).thenReturn(5);

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "mala", null, null))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        verify(userRepositoryPort).save(user);
    }

    @Test
    void cuentaBloqueadaDa423AunqueLaContrasenaSeaCorrecta() {
        user.setStatus(UserStatus.BLOCKED);
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "SecurePass123!", null, null))
                .isInstanceOf(AccountBlockedException.class);
        verify(passwordHasherPort, never()).matches(any(), any());
    }
}

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

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.AccountBlockedException;
import com.rentamovil.iam.domain.exception.EmailNotVerifiedException;
import com.rentamovil.iam.domain.exception.InvalidCredentialsException;
import com.rentamovil.iam.domain.port.out.LoginAttemptPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private LoginAttemptPort loginAttemptPort;
    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private User user;

    @BeforeEach
    void setUp() {
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
    void successfulLoginWithEmailReturnsTheTokens() {
        AuthTokens tokens = AuthTokens.builder().accessToken("jwt").refreshToken("refresh").build();
        when(userRepositoryPort.findByEmail("maria@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("SecurePass123!", "$2a$12$hash")).thenReturn(true);
        when(authTokenService.issue(user, "127.0.0.1", "test")).thenReturn(tokens);

        AuthTokens result = loginUseCase.login("Maria@Example.com", "SecurePass123!", "127.0.0.1", "test");

        assertThat(result.getAccessToken()).isEqualTo("jwt");
        assertThat(user.getLastLogin()).isNotNull();
        verify(loginAttemptPort).reset(1);
    }

    @Test
    void nonexistentUserReturns401() {
        when(userRepositoryPort.findByUsername("doesnotexist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login("doesnotexist", "SecurePass123!", null, null))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void incorrectPasswordReturns401AndCountsTheAttempt() {
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong", "$2a$12$hash")).thenReturn(false);
        when(loginAttemptPort.registerFailure(1)).thenReturn(1);

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "wrong", null, null))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(authTokenService, never()).issue(any(), any(), any());
    }

    @Test
    void theFifthFailureBlocksTheAccount() {
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrong", "$2a$12$hash")).thenReturn(false);
        when(loginAttemptPort.registerFailure(1)).thenReturn(5);

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "wrong", null, null))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        verify(userRepositoryPort).save(user);
    }

    @Test
    void blockedAccountReturns423EvenWithTheCorrectPassword() {
        user.setStatus(UserStatus.BLOCKED);
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "SecurePass123!", null, null))
                .isInstanceOf(AccountBlockedException.class);
        verify(passwordHasherPort, never()).matches(any(), any());
    }

    @Test
    void accountPendingVerificationWithCorrectPasswordGivesEmailNotVerified() {
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepositoryPort.findByUsername("mgarcia")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("SecurePass123!", "$2a$12$hash")).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.login("mgarcia", "SecurePass123!", null, null))
                .isInstanceOf(EmailNotVerifiedException.class);
        verify(authTokenService, never()).issue(any(), any(), any());
    }
}

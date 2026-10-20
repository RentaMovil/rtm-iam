package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private AuditPort auditPort;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    private User user;
    private VerificationCode code;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).person(Person.builder().email("maria@example.com").build()).passwordHash("old").build();
        code = VerificationCode.builder().id(7).userId(1).code("123456").type(CodeType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).used(false).build();
        when(userRepositoryPort.findByEmail("maria@example.com")).thenReturn(Optional.of(user));
        when(verificationCodeRepositoryPort.findLatestUnused(1, CodeType.PASSWORD_RESET)).thenReturn(Optional.of(code));
    }

    @Test
    void correctCodeChangesThePasswordAndClosesTheSessions() {
        when(passwordHasherPort.hash("NewPass123")).thenReturn("new-hash");

        resetPasswordUseCase.resetPassword("Maria@Example.com", "123456", "NewPass123");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(code.isUsed()).isTrue();
        verify(sessionRepositoryPort).revokeAllByUserId(1);
    }

    @Test
    void incorrectCodeChangesNothing() {
        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword("maria@example.com", "000000", "NewPass123"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .extracting("code").isEqualTo("INVALID_CODE");
        assertThat(user.getPasswordHash()).isEqualTo("old");
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void expiredCodeGivesCodeExpired() {
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword("maria@example.com", "123456", "NewPass123"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .extracting("code").isEqualTo("CODE_EXPIRED");
        verify(userRepositoryPort, never()).save(any());
    }
}

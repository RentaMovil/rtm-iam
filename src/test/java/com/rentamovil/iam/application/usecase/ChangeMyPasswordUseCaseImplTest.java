package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
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

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.BusinessRuleException;
import com.rentamovil.iam.domain.exception.IncorrectPasswordException;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ChangeMyPasswordUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private AuditPort auditPort;

    @InjectMocks
    private ChangeMyPasswordUseCaseImpl changeMyPasswordUseCase;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).passwordHash("old-hash").build();
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));
    }

    @Test
    void changesThePasswordAndClosesAllSessions() {
        when(passwordHasherPort.matches("Current123", "old-hash")).thenReturn(true);
        when(passwordHasherPort.matches("New1234", "old-hash")).thenReturn(false);
        when(passwordHasherPort.hash("New1234")).thenReturn("new-hash");

        changeMyPasswordUseCase.changePassword(1, "Current123", "New1234");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepositoryPort).save(user);
        verify(sessionRepositoryPort).revokeAllByUserId(1);
        verify(auditPort).record(eq(1), eq("PASSWORD_CHANGED"), eq("user"), eq(1), anyMap());
    }

    @Test
    void rejectsWhenTheCurrentPasswordIsIncorrect() {
        when(passwordHasherPort.matches("Wrong123", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> changeMyPasswordUseCase.changePassword(1, "Wrong123", "New1234"))
                .isInstanceOf(IncorrectPasswordException.class);

        verify(userRepositoryPort, never()).save(any());
        verify(sessionRepositoryPort, never()).revokeAllByUserId(any());
    }

    @Test
    void rejectsWhenTheNewPasswordEqualsTheCurrent() {
        when(passwordHasherPort.matches("Current123", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> changeMyPasswordUseCase.changePassword(1, "Current123", "Current123"))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepositoryPort, never()).save(any());
    }
}

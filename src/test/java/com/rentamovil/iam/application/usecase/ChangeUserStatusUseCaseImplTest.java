package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.common.exception.BusinessRuleException;
import com.rentamovil.common.exception.ForbiddenException;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ChangeUserStatusUseCaseImplTest {

    private static final AuthenticatedUser SUPER_ADMIN =
            new AuthenticatedUser(1, Role.SUPER_ADMIN, List.of("users:update-status"));

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private AuditPort auditPort;

    @InjectMocks
    private ChangeUserStatusUseCaseImpl changeUserStatusUseCase;

    @Test
    void desbloqueaUnaCuentaBloqueada() {
        User blocked = User.builder().id(5).status(UserStatus.BLOCKED).build();
        when(userRepositoryPort.findById(5)).thenReturn(Optional.of(blocked));
        when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = changeUserStatusUseCase.changeStatus(SUPER_ADMIN, 5, UserStatus.ACTIVE);

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(auditPort).record(1, "USER_UNBLOCKED", "user", 5, Map.of("oldStatus", "BLOCKED", "newStatus", "ACTIVE"));
    }

    @Test
    void noSePuedeDesactivarUnaCuentaBloqueada() {
        when(userRepositoryPort.findById(5)).thenReturn(Optional.of(User.builder().id(5).status(UserStatus.BLOCKED).build()));

        assertThatThrownBy(() -> changeUserStatusUseCase.changeStatus(SUPER_ADMIN, 5, UserStatus.INACTIVE))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void unClienteNoPuedeCambiarEstados() {
        AuthenticatedUser client = new AuthenticatedUser(3, Role.CLIENT, List.of("vehicles:read"));

        assertThatThrownBy(() -> changeUserStatusUseCase.changeStatus(client, 5, UserStatus.ACTIVE))
                .isInstanceOf(ForbiddenException.class);
    }
}

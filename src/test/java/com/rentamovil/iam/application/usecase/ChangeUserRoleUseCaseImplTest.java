package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.rentamovil.iam.domain.exception.BusinessRuleException;
import com.rentamovil.iam.domain.exception.ForbiddenException;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ChangeUserRoleUseCaseImplTest {

    private static final AuthenticatedUser SUPER_ADMIN = new AuthenticatedUser(1, Role.SUPER_ADMIN, List.of("roles:grant"));
    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(2, Role.ADMIN, List.of("users:read"));

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private RoleRepositoryPort roleRepositoryPort;
    @Mock
    private AuditPort auditPort;

    @InjectMocks
    private ChangeUserRoleUseCaseImpl changeUserRoleUseCase;

    @Test
    void superAdminCanGrantAdminRoleToAClient() {
        User client = User.builder().id(5).role(Role.builder().id(1).name(Role.CLIENT).build()).build();
        when(userRepositoryPort.findById(5)).thenReturn(Optional.of(client));
        when(roleRepositoryPort.findByName(Role.ADMIN)).thenReturn(Optional.of(Role.builder().id(2).name(Role.ADMIN).build()));
        when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = changeUserRoleUseCase.changeRole(SUPER_ADMIN, 5, Role.ADMIN);

        assertThat(result.getRole().getName()).isEqualTo(Role.ADMIN);
        verify(auditPort).record(1, "ROLE_GRANTED", "user", 5, Map.of("oldRole", Role.CLIENT, "newRole", Role.ADMIN));
    }

    @Test
    void anAdminCannotChangeRoles() {
        assertThatThrownBy(() -> changeUserRoleUseCase.changeRole(ADMIN, 5, Role.ADMIN))
                .isInstanceOf(ForbiddenException.class);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void noOneCanChangeTheirOwnRole() {
        assertThatThrownBy(() -> changeUserRoleUseCase.changeRole(SUPER_ADMIN, 1, Role.CLIENT))
                .isInstanceOf(BusinessRuleException.class);
        verify(auditPort, never()).record(anyInt(), anyString(), anyString(), eq(1), anyMap());
    }
}

package com.rentamovil.iam.application.usecase;

import java.util.Map;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.Role;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.BusinessRuleException;
import com.rentamovil.iam.domain.exception.ForbiddenException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.ChangeUserRoleUseCase;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeUserRoleUseCaseImpl implements ChangeUserRoleUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final AuditPort auditPort;

    @Override
    public User changeRole(AuthenticatedUser caller, Integer userId, String roleName) {

        //roles:grant es exclusivo de SUPER_ADMIN: se verifica el permiso, no se asume por jerarquía
        if (!caller.hasPermission("roles:grant")) {
            throw new ForbiddenException();
        }

        //Un SUPER_ADMIN no se cambia su propio rol: podría quedarse el sistema sin ninguno
        if (caller.getUserId().equals(userId)) {
            throw new BusinessRuleException("CANNOT_CHANGE_OWN_ROLE", "No puedes cambiar tu propio rol");
        }

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + userId));
        Role newRole = roleRepositoryPort.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el rol " + roleName));

        String oldRole = user.getRole().getName();
        if (oldRole.equals(newRole.getName())) {
            return user;
        }

        user.setRole(newRole);
        User saved = userRepositoryPort.save(user);

        //RoleGranted: no hay otro servicio que lo escuche, se deja en la auditoría (domain-events.md)
        auditPort.record(caller.getUserId(), "ROLE_GRANTED", "user", userId,
                Map.of("oldRole", oldRole, "newRole", newRole.getName()));
        return saved;
    }
}

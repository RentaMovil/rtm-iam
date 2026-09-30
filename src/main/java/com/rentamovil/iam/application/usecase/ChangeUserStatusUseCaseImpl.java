package com.rentamovil.iam.application.usecase;

import java.util.Map;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.BusinessRuleException;
import com.rentamovil.iam.domain.exception.ForbiddenException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.ChangeUserStatusUseCase;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeUserStatusUseCaseImpl implements ChangeUserStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final AuditPort auditPort;

    @Override
    public User changeStatus(AuthenticatedUser caller, Integer userId, UserStatus newStatus) {

        if (!caller.hasPermission("users:update-status")) {
            throw new ForbiddenException();
        }

        //Nadie se desactiva a sí mismo por error
        if (caller.getUserId().equals(userId)) {
            throw new BusinessRuleException("CANNOT_CHANGE_OWN_STATUS", "No puedes cambiar el estado de tu propia cuenta");
        }

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + userId));
        UserStatus oldStatus = user.getStatus();

        //Solo hay dos cambios manuales permitidos (entities-and-rules.md):
        //BLOCKED -> ACTIVE (desbloquear) y ACTIVE -> INACTIVE (desactivar).
        //A BLOCKED solo se llega solo, con 5 intentos fallidos.
        boolean unblock = oldStatus == UserStatus.BLOCKED && newStatus == UserStatus.ACTIVE;
        boolean deactivate = oldStatus == UserStatus.ACTIVE && newStatus == UserStatus.INACTIVE;
        if (!unblock && !deactivate) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "No se puede pasar de " + oldStatus + " a " + newStatus);
        }

        user.setStatus(newStatus);
        User saved = userRepositoryPort.save(user);

        auditPort.record(caller.getUserId(), unblock ? "USER_UNBLOCKED" : "USER_DEACTIVATED", "user", userId,
                Map.of("oldStatus", oldStatus.name(), "newStatus", newStatus.name()));
        return saved;
    }
}

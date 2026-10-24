package com.rentamovil.iam.application.usecase;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.common.exception.BusinessRuleException;
import com.rentamovil.iam.domain.exception.IncorrectPasswordException;
import com.rentamovil.common.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.ChangeMyPasswordUseCase;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeMyPasswordUseCaseImpl implements ChangeMyPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final SessionRepositoryPort sessionRepositoryPort;
    private final AuditPort auditPort;

    // Igual que en el reset: los errores se lanzan antes de guardar y los 3 cambios
    // (contraseña, sesiones, auditoría) quedan juntos o no quedan
    @Override
    @Transactional
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + userId));

        //La contraseña actual confirma que quien la cambia es el dueño (no alguien con la sesión abierta)
        if (!passwordHasherPort.matches(currentPassword, user.getPasswordHash())) {
            throw new IncorrectPasswordException();
        }

        if (passwordHasherPort.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessRuleException("SAME_PASSWORD", "La nueva contraseña debe ser distinta a la actual");
        }

        user.setPasswordHash(passwordHasherPort.hash(newPassword));
        userRepositoryPort.save(user);

        //Al cambiar la contraseña se cierran todas las sesiones (security-policy.md), también la actual
        sessionRepositoryPort.revokeAllByUserId(user.getId());

        auditPort.record(user.getId(), "PASSWORD_CHANGED", "user", user.getId(), Map.of("method", "CURRENT_PASSWORD"));
    }
}

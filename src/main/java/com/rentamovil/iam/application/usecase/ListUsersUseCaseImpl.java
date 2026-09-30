package com.rentamovil.iam.application.usecase;

import java.util.List;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.ForbiddenException;
import com.rentamovil.iam.domain.port.in.ListUsersUseCase;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public List<User> listUsers(AuthenticatedUser caller, String search) {
        //ADMIN y SUPER_ADMIN tienen users:read (security-policy.md); un CLIENT no
        if (!caller.hasPermission("users:read")) {
            throw new ForbiddenException();
        }
        return userRepositoryPort.search(search == null ? "" : search.trim());
    }
}

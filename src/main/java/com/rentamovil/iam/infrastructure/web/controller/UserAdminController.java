package com.rentamovil.iam.infrastructure.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.port.in.ChangeUserRoleUseCase;
import com.rentamovil.iam.domain.port.in.ChangeUserStatusUseCase;
import com.rentamovil.iam.domain.port.in.ListUsersUseCase;
import com.rentamovil.iam.infrastructure.web.auth.CurrentUser;
import com.rentamovil.iam.infrastructure.web.dto.ChangeRoleRequest;
import com.rentamovil.iam.infrastructure.web.dto.ChangeStatusRequest;
import com.rentamovil.iam.infrastructure.web.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-IAM-005: gestión de usuarios. Los permisos se revisan en cada caso de uso.
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final ListUsersUseCase listUsersUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;

    @GetMapping
    public List<UserResponse> listUsers(@CurrentUser AuthenticatedUser currentUser,
                                        @RequestParam(required = false) String search) {
        return listUsersUseCase.listUsers(currentUser, search).stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@CurrentUser AuthenticatedUser currentUser,
                                   @PathVariable Integer id,
                                   @Valid @RequestBody ChangeRoleRequest request) {
        return UserResponse.from(changeUserRoleUseCase.changeRole(currentUser, id, request.getRole()));
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(@CurrentUser AuthenticatedUser currentUser,
                                     @PathVariable Integer id,
                                     @Valid @RequestBody ChangeStatusRequest request) {
        return UserResponse.from(changeUserStatusUseCase.changeStatus(
                currentUser, id, UserStatus.valueOf(request.getStatus())));
    }
}

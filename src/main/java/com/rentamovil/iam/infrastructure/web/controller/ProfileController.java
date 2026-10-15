package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.port.in.ChangeMyEmailUseCase;
import com.rentamovil.iam.domain.port.in.ChangeMyPasswordUseCase;
import com.rentamovil.iam.domain.port.in.GetMyProfileUseCase;
import com.rentamovil.iam.domain.port.in.UpdateMyProfileUseCase;
import com.rentamovil.iam.infrastructure.web.auth.CurrentUser;
import com.rentamovil.iam.infrastructure.web.dto.ChangeEmailRequest;
import com.rentamovil.iam.infrastructure.web.dto.ChangePasswordRequest;
import com.rentamovil.iam.infrastructure.web.dto.ProfileResponse;
import com.rentamovil.iam.infrastructure.web.dto.UpdateProfileRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-IAM-004: el usuario autenticado ve y edita SU perfil. El id sale del token, nunca de la URL,
// así nadie puede editar el perfil de otro.
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final ChangeMyEmailUseCase changeMyEmailUseCase;
    private final ChangeMyPasswordUseCase changeMyPasswordUseCase;

    @GetMapping
    public ProfileResponse getProfile(@CurrentUser AuthenticatedUser currentUser) {
        return ProfileResponse.from(getMyProfileUseCase.getProfile(currentUser.getUserId()));
    }

    @PatchMapping
    public ProfileResponse updateProfile(@CurrentUser AuthenticatedUser currentUser,
                                         @Valid @RequestBody UpdateProfileRequest request) {
        return ProfileResponse.from(updateMyProfileUseCase.updateProfile(
                currentUser.getUserId(), request.getFirstName(), request.getLastName(), request.getPhone(),
                request.getImageUrl()));
    }

    @PatchMapping("/email")
    public ProfileResponse changeEmail(@CurrentUser AuthenticatedUser currentUser,
                                       @Valid @RequestBody ChangeEmailRequest request) {
        return ProfileResponse.from(changeMyEmailUseCase.changeEmail(
                currentUser.getUserId(), request.getNewEmail(), request.getCurrentPassword()));
    }

    // Cierra todas las sesiones, también la actual: el frontend debe pedir login de nuevo
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@CurrentUser AuthenticatedUser currentUser,
                               @Valid @RequestBody ChangePasswordRequest request) {
        changeMyPasswordUseCase.changePassword(
                currentUser.getUserId(), request.getCurrentPassword(), request.getNewPassword());
    }
}

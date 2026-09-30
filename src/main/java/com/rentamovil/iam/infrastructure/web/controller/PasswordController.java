package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.port.in.RequestPasswordRecoveryUseCase;
import com.rentamovil.iam.domain.port.in.ResetPasswordUseCase;
import com.rentamovil.iam.infrastructure.web.dto.ForgotPasswordRequest;
import com.rentamovil.iam.infrastructure.web.dto.ResetPasswordRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-IAM-003: recuperar la contraseña con un código enviado al email. Endpoints públicos.
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordController {

    private final RequestPasswordRecoveryUseCase requestPasswordRecoveryUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    // Siempre 202, exista o no el email
    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        requestPasswordRecoveryUseCase.requestRecovery(request.getEmail());
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
    }
}

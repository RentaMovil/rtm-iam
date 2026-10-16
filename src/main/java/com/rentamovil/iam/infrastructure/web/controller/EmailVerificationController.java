package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.port.in.ResendEmailVerificationUseCase;
import com.rentamovil.iam.domain.port.in.VerifyEmailUseCase;
import com.rentamovil.iam.infrastructure.web.dto.AuthResponse;
import com.rentamovil.iam.infrastructure.web.dto.ResendEmailVerificationRequest;
import com.rentamovil.iam.infrastructure.web.dto.VerifyEmailRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-IAM-001: confirmar el correo con el código que llega al registrarse. Endpoints públicos,
// igual que /password/*: nadie tiene sesión todavía en este punto.
@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendEmailVerificationUseCase resendEmailVerificationUseCase;

    // Si el código es válido, activa la cuenta y devuelve tokens -- queda logueada de una vez.
    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody VerifyEmailRequest request, HttpServletRequest httpRequest) {
        AuthTokens tokens = verifyEmailUseCase.verifyEmail(
                request.getEmail(),
                request.getCode(),
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));
        return AuthResponse.from(tokens);
    }

    // Siempre 202: no revela si el email existe ni en qué estado está la cuenta
    @PostMapping("/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resend(@Valid @RequestBody ResendEmailVerificationRequest request) {
        resendEmailVerificationUseCase.resend(request.getEmail());
    }

    // La columna session.user_agent es VARCHAR(255)
    private String userAgent(HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent;
    }
}

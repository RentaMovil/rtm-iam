package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
// El api-gateway enruta /auth/** hacia iam quitando el prefijo "/auth" (StripPrefix=1), igual
// que con /password/** -> PasswordController: por eso el mapping de acá es solo "/email".
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    // Mismo esquema que AuthController: web recibe el refresh token por cookie httpOnly
    // (ADR-005), mobile sigue igual, en el body.
    private static final String CLIENT_PLATFORM_HEADER = "X-Client-Platform";
    private static final String WEB_PLATFORM = "web";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendEmailVerificationUseCase resendEmailVerificationUseCase;

    // No "final", por la misma razón que en AuthController: @Value no se copia al
    // constructor que genera @RequiredArgsConstructor para un campo final.
    @Value("${app.iam.refresh-token-expiry}")
    private long refreshTokenExpirySeconds;

    @Value("${app.iam.refresh-cookie.secure}")
    private boolean refreshCookieSecure;

    // Si el código es válido, activa la cuenta y devuelve tokens -- queda logueada de una vez.
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerifyEmailRequest request,
                                                @RequestHeader(value = CLIENT_PLATFORM_HEADER, required = false) String clientPlatform,
                                                HttpServletRequest httpRequest) {
        AuthTokens tokens = verifyEmailUseCase.verifyEmail(
                request.getEmail(),
                request.getCode(),
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));

        boolean isWeb = WEB_PLATFORM.equalsIgnoreCase(clientPlatform);
        AuthResponse body = AuthResponse.from(tokens, isWeb);

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (isWeb) {
            ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, tokens.getRefreshToken())
                    .httpOnly(true)
                    .secure(refreshCookieSecure)
                    .sameSite("Strict")
                    .path(REFRESH_COOKIE_PATH)
                    .maxAge(refreshTokenExpirySeconds)
                    .build();
            response.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return response.body(body);
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

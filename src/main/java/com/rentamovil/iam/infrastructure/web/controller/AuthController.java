package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.port.in.LoginUseCase;
import com.rentamovil.iam.domain.port.in.LogoutUseCase;
import com.rentamovil.iam.domain.port.in.RefreshTokenUseCase;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.infrastructure.web.auth.CurrentUser;
import com.rentamovil.iam.infrastructure.web.dto.AuthResponse;
import com.rentamovil.iam.infrastructure.web.dto.LoginRequest;
import com.rentamovil.iam.infrastructure.web.dto.LogoutRequest;
import com.rentamovil.iam.infrastructure.web.dto.RefreshRequest;
import com.rentamovil.iam.infrastructure.web.dto.RegisterRequest;
import com.rentamovil.iam.infrastructure.web.dto.RegisterResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    // HU-IAM-001: registro público, siempre crea un CLIENT.
    // Queda PENDING_VERIFICATION: no se emite sesión todavía, primero tiene que confirmar el
    // código que se le manda al correo (POST /auth/email/verify, ese sí la deja logueada).
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        Person person = Person.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail())
                .phone(request.getPhone() == null || request.getPhone().isBlank() ? null : request.getPhone().trim())
                .build();

        User user = registerUserUseCase.register(person, request.getUsername(), request.getPassword());

        return RegisterResponse.builder()
                .email(user.getPerson().getEmail())
                .message("Revisa tu correo para verificar tu cuenta")
                .build();
    }

    // HU-IAM-002: login con email o username
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthTokens tokens = loginUseCase.login(
                request.getIdentifier(),
                request.getPassword(),
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));
        return AuthResponse.from(tokens);
    }

    // Renueva los tokens. El refresh token enviado deja de servir (rotación).
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        AuthTokens tokens = refreshTokenUseCase.refresh(
                request.getRefreshToken(),
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));
        return AuthResponse.from(tokens);
    }

    // Requiere el access token (Authorization: Bearer ...) para saber quién cierra sesión
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CurrentUser AuthenticatedUser currentUser,
                       @RequestBody(required = false) LogoutRequest request) {
        LogoutRequest body = request != null ? request : new LogoutRequest();
        logoutUseCase.logout(currentUser.getUserId(), body.getRefreshToken(), body.isAllDevices());
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

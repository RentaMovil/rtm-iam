package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.InvalidTokenException;
import com.rentamovil.iam.domain.port.in.LoginUseCase;
import com.rentamovil.iam.domain.port.in.LogoutUseCase;
import com.rentamovil.iam.domain.port.in.RefreshTokenUseCase;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;
import com.rentamovil.iam.infrastructure.web.dto.AuthResponse;
import com.rentamovil.iam.infrastructure.web.dto.LoginRequest;
import com.rentamovil.iam.infrastructure.web.dto.LogoutRequest;
import com.rentamovil.iam.infrastructure.web.dto.RefreshRequest;
import com.rentamovil.iam.infrastructure.web.dto.RegisterRequest;

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
    private final TokenProviderPort tokenProviderPort;

    // HU-IAM-001: registro público, siempre crea un CLIENT.
    // Al registrarse queda con la sesión iniciada: se crea la cuenta y enseguida se hace el login,
    // así se reutiliza todo (permisos, JWT, sesión) sin repetir código.
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        Person person = Person.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail())
                .build();

        User user = registerUserUseCase.register(person, request.getUsername(), request.getPassword());

        AuthTokens tokens = loginUseCase.login(
                user.getUsername(),
                request.getPassword(),
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));
        return AuthResponse.from(tokens);
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

    // Requiere el access token en "Authorization: Bearer ..." para saber quién cierra sesión
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                       @RequestBody(required = false) LogoutRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidTokenException("Falta el token de acceso");
        }
        Integer userId = tokenProviderPort.getUserIdFromAccessToken(authorization.substring("Bearer ".length()));

        LogoutRequest body = request != null ? request : new LogoutRequest();
        logoutUseCase.logout(userId, body.getRefreshToken(), body.isAllDevices());
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

package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.InvalidTokenException;
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

    // ADR-005: el refresh token del front WEB viaja en una cookie httpOnly, no en el body,
    // para que un XSS no pueda leerlo con JS. Mobile sigue igual que siempre (SecureStore),
    // así que solo entra en este modo cuando el cliente se identifica con este header.
    private static final String CLIENT_PLATFORM_HEADER = "X-Client-Platform";
    private static final String WEB_PLATFORM = "web";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    // Mismo alcance que ve el navegador: nginx/Vite exponen este endpoint como /api/auth/**
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    // No son "final": @RequiredArgsConstructor no copia @Value al parámetro del constructor
    // que genera, así que Spring no sabría qué inyectar ahí. Spring los llena por reflección
    // después de construir el bean, igual que SmtpEmailSenderAdapter.
    @Value("${app.iam.refresh-token-expiry}")
    private long refreshTokenExpirySeconds;

    // Cookies con Secure=true no se envían por HTTP plano: en local (sin HTTPS) hay que
    // apagarlo, en producción (rentamovil.tech, con TLS) siempre debe quedar en true.
    @Value("${app.iam.refresh-cookie.secure}")
    private boolean refreshCookieSecure;

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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               @RequestHeader(value = CLIENT_PLATFORM_HEADER, required = false) String clientPlatform,
                                               HttpServletRequest httpRequest) {
        AuthTokens tokens = loginUseCase.login(
                request.getIdentifier(),
                request.getPassword(),
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));
        return authResponseWithCookie(tokens, clientPlatform);
    }

    // Renueva los tokens. El refresh token enviado deja de servir (rotación).
    // Web lo manda por cookie; mobile sigue mandándolo en el body, como siempre.
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie,
                                                 @RequestBody(required = false) RefreshRequest request,
                                                 @RequestHeader(value = CLIENT_PLATFORM_HEADER, required = false) String clientPlatform,
                                                 HttpServletRequest httpRequest) {
        String refreshToken = resolveRefreshToken(refreshTokenCookie, request == null ? null : request.getRefreshToken());
        AuthTokens tokens = refreshTokenUseCase.refresh(
                refreshToken,
                httpRequest.getRemoteAddr(),
                userAgent(httpRequest));
        return authResponseWithCookie(tokens, clientPlatform);
    }

    // Requiere el access token (Authorization: Bearer ...) para saber quién cierra sesión
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser AuthenticatedUser currentUser,
                       @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie,
                       @RequestBody(required = false) LogoutRequest request,
                       @RequestHeader(value = CLIENT_PLATFORM_HEADER, required = false) String clientPlatform) {
        LogoutRequest body = request != null ? request : new LogoutRequest();
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie : body.getRefreshToken();
        logoutUseCase.logout(currentUser.getUserId(), refreshToken, body.isAllDevices());

        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.NO_CONTENT);
        if (WEB_PLATFORM.equalsIgnoreCase(clientPlatform)) {
            response.header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString());
        }
        return response.build();
    }

    // Body opcional (mobile) + cookie opcional (web): tiene que venir uno de los dos.
    private String resolveRefreshToken(String refreshTokenCookie, String refreshTokenBody) {
        String token = refreshTokenCookie != null ? refreshTokenCookie : refreshTokenBody;
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("No se recibió el refresh token");
        }
        return token;
    }

    // Web: el refresh token va solo en la cookie httpOnly, nunca en el body (ADR-005).
    // Cualquier otro cliente (mobile): se mantiene igual que siempre, en el body JSON.
    private ResponseEntity<AuthResponse> authResponseWithCookie(AuthTokens tokens, String clientPlatform) {
        AuthResponse body = AuthResponse.from(tokens, WEB_PLATFORM.equalsIgnoreCase(clientPlatform));

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (WEB_PLATFORM.equalsIgnoreCase(clientPlatform)) {
            response.header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.getRefreshToken()).toString());
        }
        return response.body(body);
    }

    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(refreshTokenExpirySeconds)
                .build();
    }

    private ResponseCookie expiredRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
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

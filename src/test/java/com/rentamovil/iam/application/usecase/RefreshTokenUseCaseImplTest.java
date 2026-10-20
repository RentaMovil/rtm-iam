package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.InvalidTokenException;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private TokenProviderPort tokenProviderPort;
    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private RefreshTokenUseCaseImpl refreshTokenUseCase;

    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.builder()
                .id(10)
                .userId(1)
                .refreshTokenHash("hash")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        when(tokenProviderPort.hashRefreshToken("token")).thenReturn("hash");
    }

    @Test
    void successfulRefreshRevokesThePreviousSessionAndIssuesNewTokens() {
        User user = User.builder().id(1).status(UserStatus.ACTIVE).build();
        when(sessionRepositoryPort.findByRefreshTokenHash("hash")).thenReturn(Optional.of(session));
        when(sessionRepositoryPort.revokeIfActive(10)).thenReturn(true);
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));
        when(authTokenService.issue(user, null, null)).thenReturn(AuthTokens.builder().refreshToken("new").build());

        AuthTokens tokens = refreshTokenUseCase.refresh("token", null, null);

        assertThat(tokens.getRefreshToken()).isEqualTo("new");
        verify(sessionRepositoryPort).revokeIfActive(10);
    }

    @Test
    void unknownTokenReturns401() {
        when(sessionRepositoryPort.findByRefreshTokenHash("hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.refresh("token", null, null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void alreadyUsedTokenClosesEveryUsersSession() {
        session.setRevoked(true);
        when(sessionRepositoryPort.findByRefreshTokenHash("hash")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> refreshTokenUseCase.refresh("token", null, null))
                .isInstanceOf(InvalidTokenException.class);
        verify(sessionRepositoryPort).revokeAllByUserId(1);
        verify(authTokenService, never()).issue(any(), any(), any());
    }

    @Test
    void expiredTokenReturns401() {
        session.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(sessionRepositoryPort.findByRefreshTokenHash("hash")).thenReturn(Optional.of(session));
        when(sessionRepositoryPort.revokeIfActive(10)).thenReturn(true);

        assertThatThrownBy(() -> refreshTokenUseCase.refresh("token", null, null))
                .isInstanceOf(InvalidTokenException.class);
        verify(authTokenService, never()).issue(any(), any(), any());
    }
}

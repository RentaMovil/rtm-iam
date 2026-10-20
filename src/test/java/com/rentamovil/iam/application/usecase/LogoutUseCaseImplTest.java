package com.rentamovil.iam.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private TokenProviderPort tokenProviderPort;

    @InjectMocks
    private LogoutUseCaseImpl logoutUseCase;

    @Test
    void closesTheSessionOfTheGivenRefreshToken() {
        when(tokenProviderPort.hashRefreshToken("token")).thenReturn("hash");
        when(sessionRepositoryPort.findByRefreshTokenHash("hash"))
                .thenReturn(Optional.of(Session.builder().id(10).userId(1).build()));

        logoutUseCase.logout(1, "token", false);

        verify(sessionRepositoryPort).revokeIfActive(10);
    }

    @Test
    void doesNotCloseAnotherUsersSession() {
        when(tokenProviderPort.hashRefreshToken("token")).thenReturn("hash");
        when(sessionRepositoryPort.findByRefreshTokenHash("hash"))
                .thenReturn(Optional.of(Session.builder().id(10).userId(2).build()));

        logoutUseCase.logout(1, "token", false);

        verify(sessionRepositoryPort, never()).revokeIfActive(any());
    }

    @Test
    void withAllDevicesClosesEverySession() {
        logoutUseCase.logout(1, null, true);

        verify(sessionRepositoryPort).revokeAllByUserId(1);
    }
}

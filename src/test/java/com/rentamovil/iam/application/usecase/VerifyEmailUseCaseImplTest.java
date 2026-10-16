package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private VerifyEmailUseCaseImpl verifyEmailUseCase;

    private User user;
    private VerificationCode code;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).person(Person.builder().email("maria@example.com").build())
                .status(UserStatus.PENDING_VERIFICATION).build();
        code = VerificationCode.builder().id(7).userId(1).code("123456").type(CodeType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).used(false).build();
        when(userRepositoryPort.findByEmail("maria@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void codigoCorrectoActivaLaCuentaYDevuelveTokens() {
        when(verificationCodeRepositoryPort.findLatestUnused(1, CodeType.EMAIL_VERIFICATION)).thenReturn(Optional.of(code));
        when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AuthTokens tokens = AuthTokens.builder().accessToken("token").build();
        when(authTokenService.issue(eq(user), eq("127.0.0.1"), eq("chrome"))).thenReturn(tokens);

        AuthTokens result = verifyEmailUseCase.verifyEmail("Maria@Example.com", "123456", "127.0.0.1", "chrome");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(code.isUsed()).isTrue();
        assertThat(result).isSameAs(tokens);
    }

    @Test
    void cuentaYaVerificadaDaCodigoInvalido() {
        user.setStatus(UserStatus.ACTIVE);

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail("maria@example.com", "123456", "127.0.0.1", "chrome"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .extracting("code").isEqualTo("INVALID_CODE");
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void codigoIncorrectoNoActivaNada() {
        when(verificationCodeRepositoryPort.findLatestUnused(1, CodeType.EMAIL_VERIFICATION)).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail("maria@example.com", "000000", "127.0.0.1", "chrome"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .extracting("code").isEqualTo("INVALID_CODE");
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void codigoVencidoDaCodeExpired() {
        code.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(verificationCodeRepositoryPort.findLatestUnused(1, CodeType.EMAIL_VERIFICATION)).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> verifyEmailUseCase.verifyEmail("maria@example.com", "123456", "127.0.0.1", "chrome"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .extracting("code").isEqualTo("CODE_EXPIRED");
        verify(userRepositoryPort, never()).save(any());
    }
}

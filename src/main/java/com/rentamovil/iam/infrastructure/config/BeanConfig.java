package com.rentamovil.iam.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.application.usecase.ChangeMyEmailUseCaseImpl;
import com.rentamovil.iam.application.usecase.ChangeMyPasswordUseCaseImpl;
import com.rentamovil.iam.application.usecase.ChangeUserRoleUseCaseImpl;
import com.rentamovil.iam.application.usecase.ChangeUserStatusUseCaseImpl;
import com.rentamovil.iam.application.usecase.ListUsersUseCaseImpl;
import com.rentamovil.iam.application.usecase.RequestPasswordRecoveryUseCaseImpl;
import com.rentamovil.iam.application.usecase.ResendEmailVerificationUseCaseImpl;
import com.rentamovil.iam.application.usecase.ResetPasswordUseCaseImpl;
import com.rentamovil.iam.application.usecase.VerifyEmailUseCaseImpl;
import com.rentamovil.iam.application.usecase.VerifyPasswordCodeUseCaseImpl;
import com.rentamovil.iam.domain.port.in.RequestPasswordRecoveryUseCase;
import com.rentamovil.iam.domain.port.in.ResendEmailVerificationUseCase;
import com.rentamovil.iam.domain.port.in.ResetPasswordUseCase;
import com.rentamovil.iam.domain.port.in.VerifyEmailUseCase;
import com.rentamovil.iam.domain.port.in.VerifyPasswordCodeUseCase;
import com.rentamovil.iam.domain.port.out.EmailSenderPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;
import com.rentamovil.iam.domain.port.in.ChangeUserRoleUseCase;
import com.rentamovil.iam.domain.port.in.ChangeUserStatusUseCase;
import com.rentamovil.iam.domain.port.in.ListUsersUseCase;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.application.usecase.GetMyProfileUseCaseImpl;
import com.rentamovil.iam.application.usecase.LoginUseCaseImpl;
import com.rentamovil.iam.application.usecase.UpdateMyProfileUseCaseImpl;
import com.rentamovil.iam.domain.port.in.ChangeMyEmailUseCase;
import com.rentamovil.iam.domain.port.in.ChangeMyPasswordUseCase;
import com.rentamovil.iam.domain.port.in.GetMyProfileUseCase;
import com.rentamovil.iam.domain.port.in.UpdateMyProfileUseCase;
import com.rentamovil.iam.application.usecase.LogoutUseCaseImpl;
import com.rentamovil.iam.application.usecase.RefreshTokenUseCaseImpl;
import com.rentamovil.iam.application.usecase.RegisterUserUseCaseImpl;
import com.rentamovil.iam.domain.port.in.LoginUseCase;
import com.rentamovil.iam.domain.port.in.LogoutUseCase;
import com.rentamovil.iam.domain.port.in.RefreshTokenUseCase;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.domain.port.out.LoginAttemptPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.PermissionRepositoryPort;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.RoleRepositoryPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

// Aquí se conectan los casos de uso con sus adaptadores.
// Los casos de uso no llevan @Service para que el dominio no dependa de Spring.
@Configuration
public class BeanConfig {

    @Bean
    public AuthTokenService authTokenService(PermissionRepositoryPort permissionRepositoryPort,
                                             SessionRepositoryPort sessionRepositoryPort,
                                             TokenProviderPort tokenProviderPort,
                                             @Value("${app.iam.refresh-token-expiry}") long refreshTokenExpirySeconds) {
        return new AuthTokenService(permissionRepositoryPort, sessionRepositoryPort, tokenProviderPort, refreshTokenExpirySeconds);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(PersonRepositoryPort personRepositoryPort,
                                                   UserRepositoryPort userRepositoryPort,
                                                   RoleRepositoryPort roleRepositoryPort,
                                                   PasswordHasherPort passwordHasherPort,
                                                   VerificationCodeRepositoryPort verificationCodeRepositoryPort,
                                                   EmailSenderPort emailSenderPort,
                                                   @Value("${app.iam.email-verification.code-expiry}") long codeExpirySeconds) {
        return new RegisterUserUseCaseImpl(personRepositoryPort, userRepositoryPort, roleRepositoryPort,
                passwordHasherPort, verificationCodeRepositoryPort, emailSenderPort, codeExpirySeconds);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort,
                                     PasswordHasherPort passwordHasherPort,
                                     LoginAttemptPort loginAttemptPort,
                                     AuthTokenService authTokenService) {
        return new LoginUseCaseImpl(userRepositoryPort, passwordHasherPort, loginAttemptPort, authTokenService);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(SessionRepositoryPort sessionRepositoryPort,
                                                   UserRepositoryPort userRepositoryPort,
                                                   TokenProviderPort tokenProviderPort,
                                                   AuthTokenService authTokenService) {
        return new RefreshTokenUseCaseImpl(sessionRepositoryPort, userRepositoryPort, tokenProviderPort, authTokenService);
    }

    @Bean
    public GetMyProfileUseCase getMyProfileUseCase(UserRepositoryPort userRepositoryPort) {
        return new GetMyProfileUseCaseImpl(userRepositoryPort);
    }

    @Bean
    public UpdateMyProfileUseCase updateMyProfileUseCase(UserRepositoryPort userRepositoryPort,
                                                         PersonRepositoryPort personRepositoryPort,
                                                         @Value("${app.iam.profile-image.allowed-prefix}") String allowedImageUrlPrefix) {
        return new UpdateMyProfileUseCaseImpl(userRepositoryPort, personRepositoryPort, allowedImageUrlPrefix);
    }

    @Bean
    public ChangeMyEmailUseCase changeMyEmailUseCase(UserRepositoryPort userRepositoryPort,
                                                     PersonRepositoryPort personRepositoryPort,
                                                     PasswordHasherPort passwordHasherPort) {
        return new ChangeMyEmailUseCaseImpl(userRepositoryPort, personRepositoryPort, passwordHasherPort);
    }

    @Bean
    public ChangeMyPasswordUseCase changeMyPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                                           PasswordHasherPort passwordHasherPort,
                                                           SessionRepositoryPort sessionRepositoryPort,
                                                           AuditPort auditPort) {
        return new ChangeMyPasswordUseCaseImpl(userRepositoryPort, passwordHasherPort, sessionRepositoryPort, auditPort);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepositoryPort userRepositoryPort) {
        return new ListUsersUseCaseImpl(userRepositoryPort);
    }

    @Bean
    public ChangeUserRoleUseCase changeUserRoleUseCase(UserRepositoryPort userRepositoryPort,
                                                       RoleRepositoryPort roleRepositoryPort,
                                                       AuditPort auditPort) {
        return new ChangeUserRoleUseCaseImpl(userRepositoryPort, roleRepositoryPort, auditPort);
    }

    @Bean
    public ChangeUserStatusUseCase changeUserStatusUseCase(UserRepositoryPort userRepositoryPort,
                                                           AuditPort auditPort) {
        return new ChangeUserStatusUseCaseImpl(userRepositoryPort, auditPort);
    }

    @Bean
    public RequestPasswordRecoveryUseCase requestPasswordRecoveryUseCase(
            UserRepositoryPort userRepositoryPort,
            VerificationCodeRepositoryPort verificationCodeRepositoryPort,
            EmailSenderPort emailSenderPort,
            @Value("${app.iam.password-reset.code-expiry}") long codeExpirySeconds) {
        return new RequestPasswordRecoveryUseCaseImpl(userRepositoryPort, verificationCodeRepositoryPort,
                emailSenderPort, codeExpirySeconds);
    }

    @Bean
    public VerifyPasswordCodeUseCase verifyPasswordCodeUseCase(UserRepositoryPort userRepositoryPort,
                                                               VerificationCodeRepositoryPort verificationCodeRepositoryPort) {
        return new VerifyPasswordCodeUseCaseImpl(userRepositoryPort, verificationCodeRepositoryPort);
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                                     VerificationCodeRepositoryPort verificationCodeRepositoryPort,
                                                     PasswordHasherPort passwordHasherPort,
                                                     SessionRepositoryPort sessionRepositoryPort,
                                                     AuditPort auditPort) {
        return new ResetPasswordUseCaseImpl(userRepositoryPort, verificationCodeRepositoryPort, passwordHasherPort,
                sessionRepositoryPort, auditPort);
    }

    @Bean
    public LogoutUseCase logoutUseCase(SessionRepositoryPort sessionRepositoryPort,
                                       TokenProviderPort tokenProviderPort) {
        return new LogoutUseCaseImpl(sessionRepositoryPort, tokenProviderPort);
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(UserRepositoryPort userRepositoryPort,
                                                 VerificationCodeRepositoryPort verificationCodeRepositoryPort,
                                                 AuthTokenService authTokenService) {
        return new VerifyEmailUseCaseImpl(userRepositoryPort, verificationCodeRepositoryPort, authTokenService);
    }

    @Bean
    public ResendEmailVerificationUseCase resendEmailVerificationUseCase(
            UserRepositoryPort userRepositoryPort,
            VerificationCodeRepositoryPort verificationCodeRepositoryPort,
            EmailSenderPort emailSenderPort,
            @Value("${app.iam.email-verification.code-expiry}") long codeExpirySeconds) {
        return new ResendEmailVerificationUseCaseImpl(userRepositoryPort, verificationCodeRepositoryPort,
                emailSenderPort, codeExpirySeconds);
    }
}

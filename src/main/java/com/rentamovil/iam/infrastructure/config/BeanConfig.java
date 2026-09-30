package com.rentamovil.iam.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.application.usecase.ChangeMyEmailUseCaseImpl;
import com.rentamovil.iam.application.usecase.GetMyProfileUseCaseImpl;
import com.rentamovil.iam.application.usecase.LoginUseCaseImpl;
import com.rentamovil.iam.application.usecase.UpdateMyProfileUseCaseImpl;
import com.rentamovil.iam.domain.port.in.ChangeMyEmailUseCase;
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
                                                   PasswordHasherPort passwordHasherPort) {
        return new RegisterUserUseCaseImpl(personRepositoryPort, userRepositoryPort, roleRepositoryPort, passwordHasherPort);
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
                                                         PersonRepositoryPort personRepositoryPort) {
        return new UpdateMyProfileUseCaseImpl(userRepositoryPort, personRepositoryPort);
    }

    @Bean
    public ChangeMyEmailUseCase changeMyEmailUseCase(UserRepositoryPort userRepositoryPort,
                                                     PersonRepositoryPort personRepositoryPort,
                                                     PasswordHasherPort passwordHasherPort) {
        return new ChangeMyEmailUseCaseImpl(userRepositoryPort, personRepositoryPort, passwordHasherPort);
    }

    @Bean
    public LogoutUseCase logoutUseCase(SessionRepositoryPort sessionRepositoryPort,
                                       TokenProviderPort tokenProviderPort) {
        return new LogoutUseCaseImpl(sessionRepositoryPort, tokenProviderPort);
    }
}

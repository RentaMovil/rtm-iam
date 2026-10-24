package com.rentamovil.iam.application.usecase;

import java.time.LocalDateTime;

import com.rentamovil.iam.domain.entity.PushDeviceToken;
import com.rentamovil.iam.domain.port.in.RegisterPushTokenUseCase;
import com.rentamovil.iam.domain.port.out.PushDeviceTokenRepositoryPort;

public class RegisterPushTokenUseCaseImpl implements RegisterPushTokenUseCase {

    private final PushDeviceTokenRepositoryPort pushDeviceTokenRepository;

    public RegisterPushTokenUseCaseImpl(PushDeviceTokenRepositoryPort pushDeviceTokenRepository) {
        this.pushDeviceTokenRepository = pushDeviceTokenRepository;
    }

    @Override
    public void registerToken(Integer userId, String token, String platform) {
        pushDeviceTokenRepository.save(PushDeviceToken.builder()
                .userId(userId)
                .token(token)
                .platform(platform)
                .createdAt(LocalDateTime.now())
                .build());
    }
}

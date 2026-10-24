package com.rentamovil.iam.application.usecase;

import java.util.List;

import com.rentamovil.iam.domain.entity.PushDeviceToken;
import com.rentamovil.iam.domain.port.in.GetPushTokensUseCase;
import com.rentamovil.iam.domain.port.out.PushDeviceTokenRepositoryPort;

public class GetPushTokensUseCaseImpl implements GetPushTokensUseCase {

    private final PushDeviceTokenRepositoryPort pushDeviceTokenRepository;

    public GetPushTokensUseCaseImpl(PushDeviceTokenRepositoryPort pushDeviceTokenRepository) {
        this.pushDeviceTokenRepository = pushDeviceTokenRepository;
    }

    @Override
    public List<PushDeviceToken> getTokens(Integer userId) {
        return pushDeviceTokenRepository.findByUserId(userId);
    }
}

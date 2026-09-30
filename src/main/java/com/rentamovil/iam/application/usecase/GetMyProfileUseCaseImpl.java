package com.rentamovil.iam.application.usecase;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.GetMyProfileUseCase;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetMyProfileUseCaseImpl implements GetMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User getProfile(Integer userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + userId));
    }
}

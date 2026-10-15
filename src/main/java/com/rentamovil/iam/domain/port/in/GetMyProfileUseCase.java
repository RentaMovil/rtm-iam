package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.User;

// Ver mi perfil (HU-IAM-004)
public interface GetMyProfileUseCase {

    User getProfile(Integer userId);
}

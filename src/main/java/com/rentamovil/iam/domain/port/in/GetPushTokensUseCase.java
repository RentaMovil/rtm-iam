package com.rentamovil.iam.domain.port.in;

import java.util.List;

import com.rentamovil.iam.domain.entity.PushDeviceToken;

// Para GET /internal/users/{id}/push-tokens (otro servicio componiendo a quién mandarle un push).
public interface GetPushTokensUseCase {

    List<PushDeviceToken> getTokens(Integer userId);
}

package com.rentamovil.iam.domain.port.out;

import java.util.List;

import com.rentamovil.iam.domain.entity.PushDeviceToken;

public interface PushDeviceTokenRepositoryPort {

    // Inserta, o actualiza el dueño si ese mismo token de Expo ya estaba registrado
    // (reinstalar la app / loguearse con otro usuario en el mismo dispositivo).
    PushDeviceToken save(PushDeviceToken deviceToken);

    List<PushDeviceToken> findByUserId(Integer userId);
}

package com.rentamovil.iam.infrastructure.persistence.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.entity.PushDeviceToken;
import com.rentamovil.iam.domain.port.out.PushDeviceTokenRepositoryPort;
import com.rentamovil.iam.infrastructure.persistence.entity.PushDeviceTokenEntity;
import com.rentamovil.iam.infrastructure.persistence.repository.PushDeviceTokenJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PushDeviceTokenRepositoryAdapter implements PushDeviceTokenRepositoryPort {

    private final PushDeviceTokenJpaRepository pushDeviceTokenJpaRepository;

    @Override
    public PushDeviceToken save(PushDeviceToken deviceToken) {
        // Si ese token de Expo ya estaba registrado (reinstalo la app, o inicio sesion otro
        // usuario en el mismo dispositivo), se actualiza el dueño en vez de duplicar la fila.
        PushDeviceTokenEntity entity = pushDeviceTokenJpaRepository.findByToken(deviceToken.getToken())
                .orElseGet(PushDeviceTokenEntity::new);
        entity.setUserId(deviceToken.getUserId());
        entity.setToken(deviceToken.getToken());
        entity.setPlatform(deviceToken.getPlatform());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(deviceToken.getCreatedAt());
        }
        return PersistenceMapper.toDomain(pushDeviceTokenJpaRepository.save(entity));
    }

    @Override
    public List<PushDeviceToken> findByUserId(Integer userId) {
        return pushDeviceTokenJpaRepository.findByUserId(userId).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}

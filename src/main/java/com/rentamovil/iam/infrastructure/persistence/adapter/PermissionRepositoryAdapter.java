package com.rentamovil.iam.infrastructure.persistence.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.port.out.PermissionRepositoryPort;
import com.rentamovil.iam.infrastructure.persistence.repository.PermissionJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepositoryPort {

    private final PermissionJpaRepository permissionJpaRepository;

    @Override
    public List<String> findNamesByRoleId(Integer roleId) {
        return permissionJpaRepository.findNamesByRoleId(roleId);
    }
}

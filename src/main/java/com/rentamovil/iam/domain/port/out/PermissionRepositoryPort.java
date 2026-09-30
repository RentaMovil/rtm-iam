package com.rentamovil.iam.domain.port.out;

import java.util.List;

// Permisos de un rol (iam.role_permission). Solo lectura: los siembra rtm-iam-db.
public interface PermissionRepositoryPort {

    List<String> findNamesByRoleId(Integer roleId);
}

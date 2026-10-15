package com.rentamovil.iam.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.iam.infrastructure.persistence.entity.PermissionEntity;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, Integer> {

    @Query(value = """
            SELECT p.name
            FROM iam.permission p
            JOIN iam.role_permission rp ON rp.permission_id = p.permission_id
            WHERE rp.role_id = :roleId
            ORDER BY p.name
            """, nativeQuery = true)
    List<String> findNamesByRoleId(@Param("roleId") Integer roleId);
}

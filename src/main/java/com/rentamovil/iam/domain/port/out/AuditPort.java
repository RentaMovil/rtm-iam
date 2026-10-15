package com.rentamovil.iam.domain.port.out;

import java.util.Map;

// Registra acciones importantes en shared.audit (quién hizo qué, sobre qué registro y cuándo)
public interface AuditPort {

    // detail se guarda como JSON, ej. {"oldRole": "CLIENT", "newRole": "ADMIN"}
    void record(Integer actorUserId, String action, String affectedTable, Integer affectedRecordId,
                Map<String, Object> detail);
}

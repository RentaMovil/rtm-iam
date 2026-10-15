package com.rentamovil.iam.infrastructure.persistence.adapter;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.infrastructure.persistence.entity.AuditEntity;
import com.rentamovil.iam.infrastructure.persistence.repository.AuditJpaRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class AuditAdapter implements AuditPort {

    private final AuditJpaRepository auditJpaRepository;
    private final JsonMapper jsonMapper;

    @Override
    public void record(Integer actorUserId, String action, String affectedTable, Integer affectedRecordId,
                       Map<String, Object> detail) {
        AuditEntity audit = new AuditEntity();
        audit.setUserId(actorUserId);
        audit.setAction(action);
        audit.setAffectedTable(affectedTable);
        audit.setAffectedRecordId(affectedRecordId);
        audit.setDate(LocalDateTime.now());
        //La columna detail tiene un CHECK con ISJSON(): se guarda JSON válido
        audit.setDetail(detail == null || detail.isEmpty() ? null : jsonMapper.writeValueAsString(detail));
        auditJpaRepository.save(audit);
    }
}

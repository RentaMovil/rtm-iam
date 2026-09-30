package com.rentamovil.iam.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// shared.audit: la tabla de auditoría que escriben todos los servicios (rtm-iam-db la crea).
// iam_app solo tiene INSERT sobre ella: aquí solo se agregan filas, nunca se leen ni se editan.
@Entity
@Table(schema = "shared", name = "audit")
@Getter
@Setter
@NoArgsConstructor
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer id;

    // Quién hizo la acción (null = el sistema)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "affected_table", nullable = false, length = 50)
    private String affectedTable;

    @Column(name = "affected_record_id")
    private Integer affectedRecordId;

    @Column(name = "date", nullable = false, columnDefinition = "datetime")
    private LocalDateTime date;

    @Column(name = "detail", columnDefinition = "nvarchar(max)")
    private String detail;
}

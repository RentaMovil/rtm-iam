package com.rentamovil.iam.infrastructure.web;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liveness + database check: 200 when the shared rentamovil database answers, 503 otherwise, so
 * docker compose and the gateway can tell a running-but-disconnected service from a healthy one.
 */
@RestController
public class HealthController {

    private static final int DB_CHECK_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        boolean dbUp = isDatabaseUp();
        Map<String, String> body = Map.of(
                "status", dbUp ? "ok" : "degraded",
                "db", dbUp ? "connected" : "disconnected",
                "timestamp", Instant.now().toString());
        return ResponseEntity.status(dbUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(DB_CHECK_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            return false;
        }
    }
}

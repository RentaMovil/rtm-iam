package com.rentamovil.iam.infrastructure.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.port.out.LoginAttemptPort;

// Contador de intentos fallidos en memoria.
// Limitación: se reinicia si el servicio se reinicia y no se comparte entre varias instancias.
// La doc pide Redis (02-iam/decisions.md): cuando exista, se reemplaza solo este adaptador.
@Component
public class InMemoryLoginAttemptAdapter implements LoginAttemptPort {

    private final Map<Integer, Integer> failuresByUser = new ConcurrentHashMap<>();

    @Override
    public int registerFailure(Integer userId) {
        return failuresByUser.merge(userId, 1, Integer::sum);
    }

    @Override
    public void reset(Integer userId) {
        failuresByUser.remove(userId);
    }
}

package com.rentamovil.iam.domain.port.out;

// Cuenta los intentos fallidos seguidos de cada usuario.
// La doc lo pone en Redis (02-iam/decisions.md); por ahora el adaptador es en memoria.
public interface LoginAttemptPort {

    // Suma un intento fallido y devuelve cuántos lleva seguidos
    int registerFailure(Integer userId);

    void reset(Integer userId);
}

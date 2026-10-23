package com.rentamovil.iam.domain.port.in;

// Reenvía el código de verificación de cuenta, si el código anterior venció o no llegó.
public interface ResendEmailVerificationUseCase {

    void resend(String email);
}

package com.rentamovil.iam.domain.port.out;

// Contrato para enviar emails. Por ahora el adaptador solo escribe en el log (no hay SMTP);
// cuando haya un servidor de correo se cambia solo el adaptador.
public interface EmailSenderPort {

    void sendPasswordResetCode(String email, String firstName, String code);

    void sendEmailVerificationCode(String email, String firstName, String code);
}

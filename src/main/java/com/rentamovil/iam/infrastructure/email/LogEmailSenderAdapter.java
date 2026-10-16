package com.rentamovil.iam.infrastructure.email;

import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.port.out.EmailSenderPort;

import lombok.extern.slf4j.Slf4j;

// SOLO PARA DESARROLLO: en vez de enviar el email, escribe el código en el log para poder probar.
// Nunca usar así en producción (el log quedaría con códigos válidos). Cuando haya SMTP se crea
// otro adaptador (ej. SmtpEmailSenderAdapter con spring-boot-starter-mail) y se quita este.
@Slf4j
// @Component  // Deshabilitado: usar SmtpEmailSenderAdapter para envío real de emails
public class LogEmailSenderAdapter implements EmailSenderPort {

    @Override
    public void sendPasswordResetCode(String email, String firstName, String code) {
        log.warn("[EMAIL DE DESARROLLO] Para: {} | Hola {}, tu código para recuperar la contraseña es {} (vence en 15 minutos)",
                email, firstName, code);
    }

    @Override
    public void sendEmailVerificationCode(String email, String firstName, String code) {
        log.warn("[EMAIL DE DESARROLLO] Para: {} | Hola {}, tu código para verificar tu cuenta es {} (vence en 15 minutos)",
                email, firstName, code);
    }
}

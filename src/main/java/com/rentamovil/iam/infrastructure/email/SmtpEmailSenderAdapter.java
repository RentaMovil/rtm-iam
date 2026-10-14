package com.rentamovil.iam.infrastructure.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.port.out.EmailSenderPort;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador para envío real de emails vía SMTP.
 * 
 * Reemplaza a LogEmailSenderAdapter cuando hay un servidor SMTP configurado.
 * Soporta Gmail, SendGrid, Mailgun y otros proveedores SMTP estándar.
 * 
 * Configuración requerida en application.properties:
 * - spring.mail.host
 * - spring.mail.port
 * - spring.mail.username
 * - spring.mail.password
 * - app.mail.from.email
 * - app.mail.from.name
 * 
 * IMPORTANTE: Comentar @Component en esta clase para usar LogEmailSenderAdapter,
 * o comentar @Component en LogEmailSenderAdapter para usar este.
 */
@Slf4j
@Component  // Descomentar cuando esté listo para usar envío real de emails
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from.email}")
    private String fromEmail;

    @Value("${app.mail.from.name}")
    private String fromName;

    @Override
    public void sendPasswordResetCode(String toEmail, String firstName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Código de recuperación de contraseña - RentaMovil");
            helper.setText(buildEmailBody(firstName, code), true); // true = HTML

            mailSender.send(message);
            
            log.info("Email de recuperación enviado exitosamente a: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", toEmail, e.getMessage(), e);
            // En producción podrías lanzar una excepción custom o implementar retry logic
            throw new RuntimeException("Error al enviar email de recuperación", e);
        } catch (Exception e) {
            log.error("Error inesperado al enviar email a {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al enviar email", e);
        }
    }

    /**
     * Construye el cuerpo HTML del email de recuperación de contraseña.
     * 
     * @param firstName Nombre del usuario
     * @param code Código de verificación de 6 dígitos
     * @return HTML del email
     */
    private String buildEmailBody(String firstName, String code) {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "  <title>Recuperación de Contraseña</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>" +
                "  <table role='presentation' style='width: 100%; border-collapse: collapse;'>" +
                "    <tr>" +
                "      <td align='center' style='padding: 40px 0;'>" +
                "        <table role='presentation' style='width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "          <!-- Header -->" +
                "          <tr>" +
                "            <td style='padding: 40px 40px 20px; text-align: center; background: linear-gradient(135deg, #667eea 0%, #b3b930 100%); border-radius: 8px 8px 0 0;'>" +
                "              <h1 style='margin: 0; color: #ffffff; font-size: 28px;'>RentaMovil</h1>" +
                "            </td>" +
                "          </tr>" +
                "          <!-- Body -->" +
                "          <tr>" +
                "            <td style='padding: 40px;'>" +
                "              <h2 style='margin: 0 0 20px; color: #333333; font-size: 24px;'>Hola " + escapeHtml(firstName) + ",</h2>" +
                "              <p style='margin: 0 0 20px; color: #666666; font-size: 16px; line-height: 1.6;'>" +
                "                Recibimos una solicitud para restablecer la contraseña de tu cuenta en RentaMovil." +
                "              </p>" +
                "              <p style='margin: 0 0 30px; color: #666666; font-size: 16px; line-height: 1.6;'>" +
                "                Tu código de verificación es:" +
                "              </p>" +
                "              <!-- Código -->" +
                "              <table role='presentation' style='width: 100%; border-collapse: collapse;'>" +
                "                <tr>" +
                "                  <td align='center' style='padding: 20px; background-color: #f8f9fa; border-radius: 8px;'>" +
                "                    <span style='font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: \"Courier New\", monospace;'>" +
                code +
                "                    </span>" +
                "                  </td>" +
                "                </tr>" +
                "              </table>" +
                "              <p style='margin: 30px 0 20px; color: #666666; font-size: 16px; line-height: 1.6;'>" +
                "                Este código expira en <strong>15 minutos</strong>." +
                "              </p>" +
                "              <p style='margin: 0 0 20px; color: #666666; font-size: 14px; line-height: 1.6;'>" +
                "                Si no solicitaste este cambio, puedes ignorar este email de forma segura." +
                "              </p>" +
                "            </td>" +
                "          </tr>" +
                "          <!-- Footer -->" +
                "          <tr>" +
                "            <td style='padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 8px 8px; text-align: center;'>" +
                "              <p style='margin: 0; color: #999999; font-size: 12px;'>" +
                "                © 2024 RentaMovil. Todos los derechos reservados." +
                "              </p>" +
                "              <p style='margin: 10px 0 0; color: #999999; font-size: 12px;'>" +
                "                Este es un email automático, por favor no responder." +
                "              </p>" +
                "            </td>" +
                "          </tr>" +
                "        </table>" +
                "      </td>" +
                "    </tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }

    /**
     * Escapa caracteres HTML para prevenir XSS en el nombre del usuario.
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}

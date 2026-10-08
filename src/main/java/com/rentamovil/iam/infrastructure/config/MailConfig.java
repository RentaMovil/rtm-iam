package com.rentamovil.iam.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuración de JavaMailSender para envío de emails vía SMTP.
 * 
 * Esta configuración se activa solo cuando SmtpEmailSenderAdapter está habilitado.
 * Si usas LogEmailSenderAdapter (desarrollo), este bean no se usa.
 * 
 * Variables de entorno requeridas:
 * - SMTP_HOST (ej: smtp.gmail.com, smtp.sendgrid.net)
 * - SMTP_PORT (ej: 587 para TLS, 465 para SSL)
 * - SMTP_USERNAME (email completo o 'apikey' para SendGrid)
 * - SMTP_PASSWORD (App Password para Gmail, API Key para SendGrid)
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        
        // Timeouts (5 segundos cada uno)
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        
        // Debug: cambiar a "true" para ver logs detallados de SMTP
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}

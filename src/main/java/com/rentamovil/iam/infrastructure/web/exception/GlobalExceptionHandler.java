package com.rentamovil.iam.infrastructure.web.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentamovil.iam.domain.exception.AccountBlockedException;
import com.rentamovil.iam.domain.exception.InvalidCredentialsException;
import com.rentamovil.iam.domain.exception.InvalidTokenException;
import com.rentamovil.iam.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.infrastructure.web.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

// Convierte las excepciones en respuestas con los códigos de error de iam.yaml
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> details = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return new ErrorResponse("VALIDATION_ERROR", "Datos de entrada inválidos", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadJson() {
        return new ErrorResponse("VALIDATION_ERROR", "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExists(ResourceAlreadyExistsException e) {
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException e) {
        return new ErrorResponse("INVALID_CREDENTIALS", e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidToken(InvalidTokenException e) {
        return new ErrorResponse("INVALID_TOKEN", e.getMessage());
    }

    @ExceptionHandler(AccountBlockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ErrorResponse handleAccountBlocked(AccountBlockedException e) {
        return new ErrorResponse("ACCOUNT_BLOCKED", e.getMessage());
    }

    // Dos registros iguales al mismo tiempo: los dos pasan el "existe?" pero la base frena al segundo
    // con uq_person_email o uq_user_username
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(DataIntegrityViolationException e) {
        String cause = String.valueOf(e.getMostSpecificCause().getMessage());
        if (cause.contains("uq_person_email")) {
            return new ErrorResponse("EMAIL_ALREADY_EXISTS", "El email ya está registrado");
        }
        if (cause.contains("uq_user_username")) {
            return new ErrorResponse("USERNAME_ALREADY_EXISTS", "El username ya está en uso");
        }
        log.error("Violación de integridad no esperada", e);
        return new ErrorResponse("CONFLICT", "El dato entra en conflicto con uno existente");
    }

    // El detalle solo va al log, nunca en la respuesta
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        log.error("Error no controlado", e);
        return new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error inesperado");
    }
}

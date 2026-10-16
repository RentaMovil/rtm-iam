package com.rentamovil.iam.infrastructure.web.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentamovil.iam.domain.exception.AccountBlockedException;
import com.rentamovil.iam.domain.exception.BusinessRuleException;
import com.rentamovil.iam.domain.exception.EmailNotVerifiedException;
import com.rentamovil.iam.domain.exception.ForbiddenException;
import com.rentamovil.iam.domain.exception.InvalidCredentialsException;
import com.rentamovil.iam.domain.exception.InvalidImageUrlException;
import com.rentamovil.iam.domain.exception.InvalidTokenException;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;
import com.rentamovil.iam.domain.exception.IncorrectPasswordException;
import com.rentamovil.iam.domain.exception.ResourceAlreadyExistsException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
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

    @ExceptionHandler(InvalidVerificationCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidCode(InvalidVerificationCodeException e) {
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(InvalidImageUrlException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidImageUrl(InvalidImageUrlException e) {
        return new ErrorResponse("INVALID_IMAGE_URL", e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e) {
        return new ErrorResponse("FORBIDDEN", e.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleBusinessRule(BusinessRuleException e) {
        return new ErrorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleIncorrectPassword(IncorrectPasswordException e) {
        return new ErrorResponse("INCORRECT_PASSWORD", e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException e) {
        return new ErrorResponse("NOT_FOUND", e.getMessage());
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

    @ExceptionHandler(EmailNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleEmailNotVerified(EmailNotVerifiedException e) {
        return new ErrorResponse("EMAIL_NOT_VERIFIED", e.getMessage());
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

    // Errores propios de Spring que ya traen su código HTTP: método no permitido (405),
    // ruta inexistente (404), Content-Type no soportado (415)... Se respeta ese código en vez de 500.
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleSpringWebError(ErrorResponseException e) {
        return springWebError(e.getStatusCode());
    }

    // El detalle solo va al log, nunca en la respuesta
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        //HttpRequestMethodNotSupportedException y similares implementan la interfaz ErrorResponse de Spring
        if (e instanceof org.springframework.web.ErrorResponse springError) {
            return springWebError(springError.getStatusCode());
        }
        log.error("Error no controlado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Ocurrió un error inesperado"));
    }

    private ResponseEntity<ErrorResponse> springWebError(HttpStatusCode status) {
        String code = switch (status.value()) {
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> "REQUEST_ERROR";
        };
        return ResponseEntity.status(status).body(new ErrorResponse(code, "La petición no es válida para esta ruta"));
    }
}

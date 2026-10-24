package com.rentamovil.iam.infrastructure.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentamovil.common.web.dto.ErrorResponse;
import com.rentamovil.common.web.exception.BaseExceptionHandler;
import com.rentamovil.iam.domain.exception.AccountBlockedException;
import com.rentamovil.iam.domain.exception.EmailNotVerifiedException;
import com.rentamovil.iam.domain.exception.IncorrectPasswordException;
import com.rentamovil.iam.domain.exception.InvalidCredentialsException;
import com.rentamovil.iam.domain.exception.InvalidImageUrlException;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;

import lombok.extern.slf4j.Slf4j;

// Turns exceptions into responses with the error codes of iam.yaml.
// Generic cases (validation, bad JSON, the common domain exceptions, Spring's own web errors,
// the unhandled-exception fallback) are handled by BaseExceptionHandler (rtm-common, ADR-011).
// Only this service's own exceptions are handled here.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

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

    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleIncorrectPassword(IncorrectPasswordException e) {
        return new ErrorResponse("INCORRECT_PASSWORD", e.getMessage());
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

    // Two identical registrations at once: both pass the "does it exist?" check, but the
    // database stops the second one with uq_person_email or uq_user_username
    @Override
    protected ErrorResponse mapIntegrityViolation(String cause) {
        if (cause.contains("uq_person_email")) {
            return new ErrorResponse("EMAIL_ALREADY_EXISTS", "The email is already registered");
        }
        if (cause.contains("uq_user_username")) {
            return new ErrorResponse("USERNAME_ALREADY_EXISTS", "The username is already in use");
        }
        return null;
    }
}

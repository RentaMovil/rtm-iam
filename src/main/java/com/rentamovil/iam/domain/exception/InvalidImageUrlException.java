package com.rentamovil.iam.domain.exception;

// La foto de perfil no es una URL de nuestra cuenta de Cloudinary. Responde 400.
public class InvalidImageUrlException extends RuntimeException {

    public InvalidImageUrlException(String message) {
        super(message);
    }
}

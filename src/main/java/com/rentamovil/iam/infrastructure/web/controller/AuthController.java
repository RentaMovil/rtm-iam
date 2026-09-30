package com.rentamovil.iam.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.port.in.RegisterUserUseCase;
import com.rentamovil.iam.infrastructure.web.dto.RegisterRequest;
import com.rentamovil.iam.infrastructure.web.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;

    // HU-IAM-001: registro público, siempre crea un CLIENT
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        Person person = Person.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail())
                .build();

        User user = registerUserUseCase.register(person, request.getUsername(), request.getPassword());
        return UserResponse.from(user);
    }
}

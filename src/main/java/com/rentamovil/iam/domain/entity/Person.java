package com.rentamovil.iam.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    // Foto de perfil: solo el enlace (la imagen vive en Cloudinary)
    private String imageUrl;
}

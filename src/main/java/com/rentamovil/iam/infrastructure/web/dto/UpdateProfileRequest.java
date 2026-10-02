package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de PATCH /users/me. Todos los campos son opcionales: lo que no venga no se cambia.
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100)
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío")
    private String firstName;

    @Size(max = 100)
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío")
    private String lastName;

    // Vacío = borrar el teléfono
    @Size(max = 20)
    @Pattern(regexp = "^[0-9+ ]*$", message = "solo números, espacios y +")
    private String phone;

    // Vacío = quitar la foto. Que sea de nuestra cuenta de Cloudinary lo revisa el caso de uso.
    @Size(max = 500)
    private String imageUrl;
}

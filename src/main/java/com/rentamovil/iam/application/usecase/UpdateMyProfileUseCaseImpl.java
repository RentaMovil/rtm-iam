package com.rentamovil.iam.application.usecase;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.InvalidImageUrlException;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.UpdateMyProfileUseCase;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateMyProfileUseCaseImpl implements UpdateMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    // Solo se aceptan fotos subidas a nuestra cuenta de Cloudinary (ej. https://res.cloudinary.com/<cuenta>/image/upload/)
    private final String allowedImageUrlPrefix;

    @Override
    public User updateProfile(Integer userId, String firstName, String lastName, String phone, String imageUrl) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + userId));
        Person person = user.getPerson();

        //Solo se cambia lo que venga en la petición
        if (firstName != null) {
            person.setFirstName(firstName.trim());
        }
        if (lastName != null) {
            person.setLastName(lastName.trim());
        }
        if (phone != null) {
            //Teléfono vacío = borrarlo (la columna acepta NULL)
            person.setPhone(phone.isBlank() ? null : phone.trim());
        }
        if (imageUrl != null) {
            //Foto vacía = quitarla
            person.setImageUrl(imageUrl.isBlank() ? null : checkImageUrl(imageUrl.trim()));
        }

        user.setPerson(personRepositoryPort.save(person));
        return user;
    }

    // Si se aceptara cualquier URL, alguien podría poner una imagen de otro sitio que se carga
    // en el navegador de quien vea su perfil (ej. los admins)
    private String checkImageUrl(String imageUrl) {
        if (!imageUrl.startsWith(allowedImageUrlPrefix)) {
            throw new InvalidImageUrlException("La foto debe subirse desde la aplicación");
        }
        return imageUrl;
    }
}

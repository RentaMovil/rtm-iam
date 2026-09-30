package com.rentamovil.iam.application.usecase;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.ResourceNotFoundException;
import com.rentamovil.iam.domain.port.in.UpdateMyProfileUseCase;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateMyProfileUseCaseImpl implements UpdateMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;

    @Override
    public User updateProfile(Integer userId, String firstName, String lastName, String phone) {
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

        user.setPerson(personRepositoryPort.save(person));
        return user;
    }
}

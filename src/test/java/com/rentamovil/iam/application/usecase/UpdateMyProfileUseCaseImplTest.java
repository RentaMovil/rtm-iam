package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.InvalidImageUrlException;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UpdateMyProfileUseCaseImplTest {

    private static final String CLOUDINARY = "https://res.cloudinary.com/rentamovil/image/upload/";

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PersonRepositoryPort personRepositoryPort;

    private UpdateMyProfileUseCaseImpl updateMyProfileUseCase;
    private User user;

    @BeforeEach
    void setUp() {
        updateMyProfileUseCase = new UpdateMyProfileUseCaseImpl(userRepositoryPort, personRepositoryPort, CLOUDINARY);
        user = User.builder()
                .id(1)
                .person(Person.builder().id(1).firstName("María").lastName("García").phone("300")
                        .imageUrl(CLOUDINARY + "vieja.jpg").build())
                .build();
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));
    }

    @Test
    void soloCambiaLosCamposQueVienen() {
        when(personRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = updateMyProfileUseCase.updateProfile(1, null, null, "3001234567", null);

        assertThat(result.getPerson().getPhone()).isEqualTo("3001234567");
        assertThat(result.getPerson().getFirstName()).isEqualTo("María");
        assertThat(result.getPerson().getLastName()).isEqualTo("García");
        assertThat(result.getPerson().getImageUrl()).isEqualTo(CLOUDINARY + "vieja.jpg");
    }

    @Test
    void cambiaLaFotoSiEsDeNuestraCuentaDeCloudinary() {
        when(personRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = updateMyProfileUseCase.updateProfile(1, null, null, null, CLOUDINARY + "v1/nueva.jpg");

        assertThat(result.getPerson().getImageUrl()).isEqualTo(CLOUDINARY + "v1/nueva.jpg");
    }

    @Test
    void fotoVaciaLaQuita() {
        when(personRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = updateMyProfileUseCase.updateProfile(1, null, null, null, "");

        assertThat(result.getPerson().getImageUrl()).isNull();
    }

    @Test
    void rechazaFotosDeOtrosSitios() {
        assertThatThrownBy(() -> updateMyProfileUseCase.updateProfile(1, null, null, null, "https://otro-sitio.com/foto.jpg"))
                .isInstanceOf(InvalidImageUrlException.class);

        verify(personRepositoryPort, never()).save(any());
    }
}

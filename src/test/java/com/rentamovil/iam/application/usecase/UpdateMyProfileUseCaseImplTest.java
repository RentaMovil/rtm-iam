package com.rentamovil.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.port.out.PersonRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UpdateMyProfileUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @InjectMocks
    private UpdateMyProfileUseCaseImpl updateMyProfileUseCase;

    @Test
    void soloCambiaLosCamposQueVienen() {
        User user = User.builder()
                .id(1)
                .person(Person.builder().id(1).firstName("María").lastName("García").phone("300").build())
                .build();
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));
        when(personRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = updateMyProfileUseCase.updateProfile(1, null, null, "3001234567");

        assertThat(result.getPerson().getPhone()).isEqualTo("3001234567");
        assertThat(result.getPerson().getFirstName()).isEqualTo("María");
        assertThat(result.getPerson().getLastName()).isEqualTo("García");
    }
}

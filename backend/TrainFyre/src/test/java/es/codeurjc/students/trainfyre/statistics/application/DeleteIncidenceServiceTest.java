package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.application.port.in.DeleteIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.application.service.DeleteIncidenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteIncidenceServiceTest {

    @Mock
    private IncidencePort incidencePort;
    private DeleteIncidenceUseCase deleteIncidenceUseCase;

    @BeforeEach
    void setUp(){
        deleteIncidenceUseCase = new DeleteIncidenceService(incidencePort);
    }

    @Test
    void shouldDeleteAnIncidenceWithSpecifiedUUID(){

        UUID id = UUID.randomUUID();

        deleteIncidenceUseCase.execute(new DeleteIncidenceCommand(id));

        verify(incidencePort).delete(id);

    }


}

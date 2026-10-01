package es.codeurjc.students.trainfyre.statistics.infrastructure;

import es.codeurjc.students.trainfyre.statistics.application.port.in.DeleteIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
class DeleteIncidenceServiceIntegrationTest {

    @Autowired
    private DeleteIncidenceUseCase deleteIncidenceUseCase;

    @MockitoBean
    private IncidencePort incidencePort;

    @Test
    void shouldDeleteAnIncidenceWithSpecifiedUUIDThroughSpringContext() {
        UUID id = UUID.randomUUID();

        Void result = deleteIncidenceUseCase.execute(new DeleteIncidenceCommand(id));

        assertThat(result).isNull();
        verify(incidencePort).delete(id);
    }
}
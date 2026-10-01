package es.codeurjc.students.trainfyre.statistics.infrastructure;

import es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder;
import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CreateIncidenceServiceIntegrationTest {

    @Autowired
    private CreateIncidenceUseCase createIncidenceUseCase;

    @MockitoBean
    private IncidencePort incidencePort;

    @Test
    void shouldCreateAndSaveAnIncidenceThroughSpringContext() {
        var affectedNetwork = IncidenceTestBuilder.generateDefaultAffectedNetwork();
        var occurrence = IncidenceTestBuilder.generateDefaultOccurrence();
        var description = IncidenceTestBuilder.generateDefaultDescription();
        var classification = IncidenceTestBuilder.generateDefaultClassification();

        var command = new CreateIncidenceCommand(
                affectedNetwork, occurrence, description, classification
        );

        UUID id = createIncidenceUseCase.execute(command);

        ArgumentCaptor<Incidence> captor = ArgumentCaptor.forClass(Incidence.class);
        verify(incidencePort).save(captor.capture());

        Incidence savedIncidence = captor.getValue();
        assertThat(id).isEqualTo(savedIncidence.getId());
        assertThat(savedIncidence.getAffectedNetwork()).isEqualTo(affectedNetwork);
        assertThat(savedIncidence.getOccurrence()).isEqualTo(occurrence);
        assertThat(savedIncidence.getDescription()).isEqualTo(description);
        assertThat(savedIncidence.getClassification()).isEqualTo(classification);
    }
}
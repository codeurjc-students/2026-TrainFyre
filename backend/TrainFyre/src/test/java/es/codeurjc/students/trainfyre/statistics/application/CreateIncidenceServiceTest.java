package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder;
import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.application.service.CreateIncidenceService;
import es.codeurjc.students.trainfyre.statistics.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CreateIncidenceServiceTest {


    @Mock
    private IncidencePort incidencePort;
    private CreateIncidenceUseCase createIncidenceUseCase;

    private final AffectedNetwork affectedNetwork = IncidenceTestBuilder.generateDefaultAffectedNetwork();
    private final Occurrence occurrence = IncidenceTestBuilder.generateDefaultOccurrence();
    private final Description description = IncidenceTestBuilder.generateDefaultDescription();
    private final Classification classification = IncidenceTestBuilder.generateDefaultClassification();
    private final CreateIncidenceCommand createIncidenceCommand = new CreateIncidenceCommand(affectedNetwork, occurrence, description, classification);

    @BeforeEach
    void setUp(){
        createIncidenceUseCase = new CreateIncidenceService(incidencePort);
    }


    @Test
    void shouldCreateAndSaveAnIncidence() {

        UUID uuid = createIncidenceUseCase.execute(createIncidenceCommand);

        ArgumentCaptor<Incidence> captor = ArgumentCaptor.forClass(Incidence.class);

        verify(incidencePort).save(captor.capture());

        Incidence savedIncidence = captor.getValue();

        assertThat(uuid).isEqualTo(savedIncidence.getId());
        assertThat(savedIncidence.getAffectedNetwork()).isEqualTo(affectedNetwork);
        assertThat(savedIncidence.getOccurrence()).isEqualTo(occurrence);
        assertThat(savedIncidence.getDescription()).isEqualTo(description);
        assertThat(savedIncidence.getClassification()).isEqualTo(classification);

    }

}

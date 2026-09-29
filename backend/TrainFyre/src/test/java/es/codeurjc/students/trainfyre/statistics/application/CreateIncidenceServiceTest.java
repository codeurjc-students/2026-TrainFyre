package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.SaveIncidencePort;
import es.codeurjc.students.trainfyre.statistics.application.service.CreateIncidenceService;
import es.codeurjc.students.trainfyre.statistics.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CreateIncidenceServiceTest {


    @Mock
    private SaveIncidencePort saveIncidencePort;
    private CreateIncidenceUseCase createIncidenceUseCase;

    private final AffectedNetwork affectedNetwork = new AffectedNetwork(1L, Arrays.asList(1L, 2L, 3L));
    private final Occurrence occurrence = new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(30));
    private final Description description = new Description("Train failure", "The train's engine has broken down and won't start.");
    private final Classification classification = new Classification(Severity.MODERATE, Cause.MEDICAL_EMERGENCY);
    private final CreateIncidenceCommand createIncidenceCommand = new CreateIncidenceCommand(affectedNetwork, occurrence, description, classification);

    @BeforeEach
    void setUp(){
        createIncidenceUseCase = new CreateIncidenceService(saveIncidencePort);
    }


    @Test
    void shouldCreateAndSaveAnIncidence() {

        UUID uuid = createIncidenceUseCase.execute(createIncidenceCommand);

        ArgumentCaptor<Incidence> captor = ArgumentCaptor.forClass(Incidence.class);

        verify(saveIncidencePort).save(captor.capture());

        Incidence savedIncidence = captor.getValue();

        assertThat(uuid).isEqualTo(savedIncidence.getId());
        assertThat(savedIncidence.getAffectedNetwork()).isEqualTo(affectedNetwork);
        assertThat(savedIncidence.getOccurrence()).isEqualTo(occurrence);
        assertThat(savedIncidence.getDescription()).isEqualTo(description);
        assertThat(savedIncidence.getClassification()).isEqualTo(classification);

    }

}

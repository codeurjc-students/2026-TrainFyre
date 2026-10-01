package es.codeurjc.students.trainfyre.statistics.infrastructure;

import es.codeurjc.students.trainfyre.statistics.application.port.in.UpdateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UpdateIncidenceServiceIntegrationTest {

    @Autowired
    private UpdateIncidenceUseCase updateIncidenceUseCase;

    @MockitoBean
    private IncidencePort incidencePort;

    private Incidence reference;

    @BeforeEach
    void setUp() {
        reference = anIncidence().build();
    }

    static Stream<Arguments> updateCases() {
        AffectedNetwork network = new AffectedNetwork(3L, List.of(1L));
        Occurrence occurrence =
                new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(50));
        Description description =
                new Description("New Description", "This is the new description!!!");
        Classification classification =
                new Classification(Severity.CRITICAL, Cause.TECHNICAL_PROBLEM);

        return Stream.of(
                Arguments.of("network",
                        (Function<UUID, UpdateIncidenceCommand>) id ->
                                new UpdateIncidenceCommand(id, network, null, null, null),
                        (Function<Incidence, Object>) Incidence::getAffectedNetwork,
                        network),
                Arguments.of("occurrence",
                        (Function<UUID, UpdateIncidenceCommand>) id ->
                                new UpdateIncidenceCommand(id, null, occurrence, null, null),
                        (Function<Incidence, Object>) Incidence::getOccurrence,
                        occurrence),
                Arguments.of("description",
                        (Function<UUID, UpdateIncidenceCommand>) id ->
                                new UpdateIncidenceCommand(id, null, null, description, null),
                        (Function<Incidence, Object>) Incidence::getDescription,
                        description),
                Arguments.of("classification",
                        (Function<UUID, UpdateIncidenceCommand>) id ->
                                new UpdateIncidenceCommand(id, null, null, null, classification),
                        (Function<Incidence, Object>) Incidence::getClassification,
                        classification)
        );
    }

    @ParameterizedTest(name = "should update {0} through Spring context")
    @MethodSource("updateCases")
    void shouldUpdateAndSaveIncidence(
            String field,
            Function<UUID, UpdateIncidenceCommand> commandFactory,
            Function<Incidence, Object> updatedField,
            Object expectedValue
    ) {
        UpdateIncidenceCommand command = commandFactory.apply(reference.getId());
        when(incidencePort.findById(command.uuid(), Incidence.class))
                .thenReturn(reference);

        Void result = updateIncidenceUseCase.execute(command);

        ArgumentCaptor<Incidence> captor = ArgumentCaptor.forClass(Incidence.class);
        verify(incidencePort).findById(command.uuid(), Incidence.class);
        verify(incidencePort).save(captor.capture());

        Incidence savedIncidence = captor.getValue();
        assertThat(result).isNull();
        assertThat(savedIncidence.getId()).isEqualTo(reference.getId());
        assertThat(updatedField.apply(savedIncidence)).isEqualTo(expectedValue);
    }
}
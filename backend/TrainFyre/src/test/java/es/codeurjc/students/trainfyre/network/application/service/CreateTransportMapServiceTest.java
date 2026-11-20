package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.CreateTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.DuplicateLineException;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTransportMapServiceTest {

    private static final String FIRST_EXTERNAL_ID = "C1";
    private static final String SECOND_EXTERNAL_ID = "C2";
    private static final MapMetadata METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-madrid"), "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final List<Line> LINES = List.of(newLine(FIRST_EXTERNAL_ID), newLine(SECOND_EXTERNAL_ID));

    @Mock
    private TransportMapPort transportMapPort;
    private CreateTransportMapUseCase createTransportMapUseCase;

    @BeforeEach
    void setUp() {
        createTransportMapUseCase = new CreateTransportMapService(transportMapPort);
    }

    @Test
    void shouldSaveMapWithCommandDataAndReturnItsIdWhenCommandIsValid() {
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, LINES, NOW);

        UUID result = createTransportMapUseCase.execute(command);

        TransportMap savedMap = captureSavedMap();
        assertThat(result).isEqualTo(savedMap.getId().value());
        assertThat(savedMap.getMapMetadata()).isEqualTo(METADATA);
        assertThat(savedMap.getLastUpdatedAt()).isEqualTo(NOW);
        assertThat(savedMap.linesByExternalId())
                .extracting(Line::getExternalId)
                .containsExactlyInAnyOrder(FIRST_EXTERNAL_ID, SECOND_EXTERNAL_ID);
    }

    @Test
    void shouldSaveMapWithoutLinesWhenCommandHasNoLines() {
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, null, NOW);

        createTransportMapUseCase.execute(command);

        assertThat(captureSavedMap().linesByExternalId()).isEmpty();
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        RuntimeException persistenceFailure = new RuntimeException("Could not save transport map");
        doThrow(persistenceFailure).when(transportMapPort).save(any(TransportMap.class));
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, LINES, NOW);

        assertThatThrownBy(() -> createTransportMapUseCase.execute(command)).isSameAs(persistenceFailure);
    }

    @Test
    void shouldPropagateDuplicateLineExceptionWithoutSavingWhenLinesShareExternalId() {
        List<Line> duplicatedLines = List.of(newLine(FIRST_EXTERNAL_ID), newLine(FIRST_EXTERNAL_ID));
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, duplicatedLines, NOW);

        assertThatThrownBy(() -> createTransportMapUseCase.execute(command))
                .isInstanceOf(DuplicateLineException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldLookUpTheSourceBeforeSaving() {
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, LINES, NOW);

        createTransportMapUseCase.execute(command);

        InOrder inOrder = inOrder(transportMapPort);
        inOrder.verify(transportMapPort).findBySource(METADATA.source());
        inOrder.verify(transportMapPort).save(any(TransportMap.class));
    }

    @Test
    void shouldThrowMapAlreadyExistsExceptionWithoutSavingWhenSourceAlreadyExists() {
        when(transportMapPort.findBySource(METADATA.source()))
                .thenReturn(Optional.of(TransportMap.createTransportMap(METADATA, NOW)));
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, LINES, NOW);

        assertThatThrownBy(() -> createTransportMapUseCase.execute(command))
                .isInstanceOf(MapAlreadyExistsException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateExceptionWithoutSavingWhenSourceLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(METADATA.source())).thenThrow(lookupFailure);
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, LINES, NOW);

        assertThatThrownBy(() -> createTransportMapUseCase.execute(command)).isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldThrowMapAlreadyExistsExceptionWhenSourceExistsAndLinesAreDuplicated() {
        when(transportMapPort.findBySource(METADATA.source()))
                .thenReturn(Optional.of(TransportMap.createTransportMap(METADATA, NOW)));
        List<Line> duplicatedLines = List.of(newLine(FIRST_EXTERNAL_ID), newLine(FIRST_EXTERNAL_ID));
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, duplicatedLines, NOW);

        assertThatThrownBy(() -> createTransportMapUseCase.execute(command))
                .isInstanceOf(MapAlreadyExistsException.class);
    }

    private TransportMap captureSavedMap() {
        ArgumentCaptor<TransportMap> captor = ArgumentCaptor.forClass(TransportMap.class);
        verify(transportMapPort).save(captor.capture());
        return captor.getValue();
    }

    private static Line newLine(String externalId) {
        return Line.createLine(externalId, new LineName(externalId, "Cercanias " + externalId), LineType.RAIL, null);
    }
}

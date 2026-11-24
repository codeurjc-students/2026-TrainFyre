package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.CreateLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DuplicateLineException;
import es.codeurjc.students.trainfyre.network.domain.HexColor;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.BY_SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenLookupFails;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenNoMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.verifyLookedUp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateLineServiceTest {

    private static final String SOURCE_METHOD = "es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs";
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final String EXTERNAL_ID = "C-1";
    private static final LineName NAME = new LineName("C1", "Cercanias C1");
    private static final LineType TYPE = LineType.RAIL;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private CreateLineUseCase createLineUseCase;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        createLineUseCase = new CreateLineService(
                transportMapPort, new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort)));
        existingMap = TransportMap.createTransportMap(METADATA, CREATED_AT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldAddTheLineToTheMapFoundByTheRefAndSaveItWhenCommandIsValid(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        Void result = createLineUseCase.execute(commandWith(ref, COLORS, NOW));

        TransportMap savedMap = captureSavedMap();
        Line line = savedMap.findLine(EXTERNAL_ID);
        assertThat(result).isNull();
        assertThat(savedMap).isSameAs(existingMap);
        assertThat(line.getId()).isNull();
        assertThat(line.getName()).isEqualTo(NAME);
        assertThat(line.getType()).isEqualTo(TYPE);
        assertThat(line.getColors()).isEqualTo(COLORS);
        assertThat(savedMap.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldCreateTheLineWithDefaultColorsWhenColorsAreNotProvided() {
        givenMapFound(transportMapPort, BY_SOURCE, existingMap);

        createLineUseCase.execute(commandWith(BY_SOURCE, null, NOW));

        assertThat(captureSavedMap().findLine(EXTERNAL_ID).getColors())
                .isEqualTo(LineColors.defaultFor(EXTERNAL_ID));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldThrowMapNotFoundExceptionWithoutSavingWhenNoMapMatchesTheRef(String scenario, MapRef ref) {
        givenNoMapFound(transportMapPort, ref);

        assertThatThrownBy(() -> createLineUseCase.execute(commandWith(ref, COLORS, NOW)))
                .isInstanceOf(MapNotFoundException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldPropagateExceptionWithoutSavingWhenTheLookupFails(String scenario, MapRef ref) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, ref, lookupFailure);

        assertThatThrownBy(() -> createLineUseCase.execute(commandWith(ref, COLORS, NOW)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldThrowDuplicateLineExceptionWithoutSavingWhenTheMapHasALineWithTheSameExternalId(
            String scenario, MapRef ref) {
        existingMap.addLine(Line.createLine(EXTERNAL_ID, NAME, TYPE, COLORS));
        givenMapFound(transportMapPort, ref, existingMap);

        assertThatThrownBy(() -> createLineUseCase.execute(commandWith(ref, COLORS, NOW)))
                .isInstanceOf(DuplicateLineException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldPropagateIllegalArgumentExceptionWithoutSavingWhenNowIsBeforeTheLastUpdate(
            String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        Instant beforeLastUpdate = CREATED_AT.minusSeconds(1);

        assertThatThrownBy(() -> createLineUseCase.execute(commandWith(ref, COLORS, beforeLastUpdate)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldPropagateExceptionWhenSavingFails(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        RuntimeException persistenceFailure = new RuntimeException("Could not save transport map");
        doThrow(persistenceFailure).when(transportMapPort).save(any(TransportMap.class));

        assertThatThrownBy(() -> createLineUseCase.execute(commandWith(ref, COLORS, NOW)))
                .isSameAs(persistenceFailure);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(SOURCE_METHOD)
    void shouldFindTheMapBeforeSavingIt(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        createLineUseCase.execute(commandWith(ref, COLORS, NOW));

        InOrder inOrder = inOrder(transportMapPort);
        verifyLookedUp(inOrder, transportMapPort, ref);
        inOrder.verify(transportMapPort).save(any(TransportMap.class));
    }

    private static CreateLineCommand commandWith(MapRef ref, LineColors colors, Instant now) {
        return new CreateLineCommand(ref, EXTERNAL_ID, NAME, TYPE, colors, now);
    }

    private TransportMap captureSavedMap() {
        ArgumentCaptor<TransportMap> captor = ArgumentCaptor.forClass(TransportMap.class);
        verify(transportMapPort).save(captor.capture());
        return captor.getValue();
    }
}

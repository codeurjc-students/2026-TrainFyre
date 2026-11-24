package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.UpdateLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.HexColor;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineChanges;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
import es.codeurjc.students.trainfyre.network.domain.LineId;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import es.codeurjc.students.trainfyre.network.domain.MapId;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.BY_ID;
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
class UpdateLineServiceTest {

    private static final String MAP_REFS =
            "es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs";
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final LineId LINE_ID = new LineId(7L);
    private static final String EXTERNAL_ID = "C-1";
    private static final LineName NAME = new LineName("C1", "Cercanias C1");
    private static final LineType TYPE = LineType.RAIL;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));
    private static final LineName NEW_NAME = new LineName("C2", "Cercanias C2");
    private static final LineChanges CHANGES = new LineChanges(NEW_NAME, null, null);
    private static final LineRef LINE_BY_ID = new LineRef.ById(LINE_ID);
    private static final LineRef LINE_BY_EXTERNAL_ID = new LineRef.ByExternalId(EXTERNAL_ID);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private UpdateLineUseCase updateLineUseCase;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        updateLineUseCase = new UpdateLineService(
                transportMapPort, new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort)));
        existingMap = TransportMap.reconstitute(MapId.generate(), METADATA, CREATED_AT,
                List.of(Line.reconstitute(LINE_ID, EXTERNAL_ID, NAME, TYPE, COLORS)));
    }

    static Stream<Arguments> mapAndLineRefs() {
        return Stream.of(
                Arguments.of("map by id, line by id", BY_ID, LINE_BY_ID),
                Arguments.of("map by id, line by external id", BY_ID, LINE_BY_EXTERNAL_ID),
                Arguments.of("map by source, line by id", BY_SOURCE, LINE_BY_ID),
                Arguments.of("map by source, line by external id", BY_SOURCE, LINE_BY_EXTERNAL_ID)
        );
    }

    static Stream<Arguments> unknownLineRefs() {
        return Stream.of(
                Arguments.of("unknown id", new LineRef.ById(new LineId(99L))),
                Arguments.of("unknown external id", new LineRef.ByExternalId("X-9"))
        );
    }

    static Stream<Arguments> existingLineRefs() {
        return Stream.of(
                Arguments.of("line by id", LINE_BY_ID),
                Arguments.of("line by external id", LINE_BY_EXTERNAL_ID)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mapAndLineRefs")
    void shouldApplyTheChangesToTheLineFoundByTheRefAndSaveTheMapWhenCommandIsValid(
            String scenario, MapRef mapRef, LineRef lineRef) {
        givenMapFound(transportMapPort, mapRef, existingMap);

        Void result = updateLineUseCase.execute(new UpdateLineCommand(mapRef, lineRef, CHANGES, NOW));

        TransportMap savedMap = captureSavedMap();
        Line line = savedMap.findLine(LINE_ID);
        assertThat(result).isNull();
        assertThat(savedMap).isSameAs(existingMap);
        assertThat(line.getName()).isEqualTo(NEW_NAME);
        assertThat(line.getType()).isEqualTo(TYPE);
        assertThat(line.getColors()).isEqualTo(COLORS);
        assertThat(line.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(savedMap.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldThrowMapNotFoundExceptionWithoutSavingWhenNoMapMatchesTheRef(String scenario, MapRef mapRef) {
        givenNoMapFound(transportMapPort, mapRef);

        assertThatThrownBy(() -> updateLineUseCase.execute(commandWith(mapRef, LINE_BY_ID, NOW)))
                .isInstanceOf(MapNotFoundException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldPropagateExceptionWithoutSavingWhenTheMapLookupFails(String scenario, MapRef mapRef) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, mapRef, lookupFailure);

        assertThatThrownBy(() -> updateLineUseCase.execute(commandWith(mapRef, LINE_BY_ID, NOW)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unknownLineRefs")
    void shouldThrowLineNotFoundExceptionWithoutSavingWhenTheMapHasNoLineMatchingTheRef(
            String scenario, LineRef lineRef) {
        givenMapFound(transportMapPort, BY_SOURCE, existingMap);

        assertThatThrownBy(() -> updateLineUseCase.execute(commandWith(BY_SOURCE, lineRef, NOW)))
                .isInstanceOf(LineNotFoundException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("existingLineRefs")
    void shouldPropagateIllegalArgumentExceptionWithoutSavingWhenNowIsBeforeTheLastUpdate(
            String scenario, LineRef lineRef) {
        givenMapFound(transportMapPort, BY_SOURCE, existingMap);
        Instant beforeLastUpdate = CREATED_AT.minusSeconds(1);

        assertThatThrownBy(() -> updateLineUseCase.execute(commandWith(BY_SOURCE, lineRef, beforeLastUpdate)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldPropagateExceptionWhenSavingFails(String scenario, MapRef mapRef) {
        givenMapFound(transportMapPort, mapRef, existingMap);
        RuntimeException persistenceFailure = new RuntimeException("Could not save transport map");
        doThrow(persistenceFailure).when(transportMapPort).save(any(TransportMap.class));

        assertThatThrownBy(() -> updateLineUseCase.execute(commandWith(mapRef, LINE_BY_ID, NOW)))
                .isSameAs(persistenceFailure);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldFindTheMapBeforeSavingIt(String scenario, MapRef mapRef) {
        givenMapFound(transportMapPort, mapRef, existingMap);

        updateLineUseCase.execute(commandWith(mapRef, LINE_BY_ID, NOW));

        InOrder inOrder = inOrder(transportMapPort);
        verifyLookedUp(inOrder, transportMapPort, mapRef);
        inOrder.verify(transportMapPort).save(any(TransportMap.class));
    }

    private static UpdateLineCommand commandWith(MapRef mapRef, LineRef lineRef, Instant now) {
        return new UpdateLineCommand(mapRef, lineRef, CHANGES, now);
    }

    private TransportMap captureSavedMap() {
        ArgumentCaptor<TransportMap> captor = ArgumentCaptor.forClass(TransportMap.class);
        verify(transportMapPort).save(captor.capture());
        return captor.getValue();
    }
}

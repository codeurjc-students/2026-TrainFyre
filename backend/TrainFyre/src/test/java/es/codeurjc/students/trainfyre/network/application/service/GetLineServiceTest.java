package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetLineQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.HexColor;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static es.codeurjc.students.trainfyre.network.application.service.LineRefFixtures.EXTERNAL_ID;
import static es.codeurjc.students.trainfyre.network.application.service.LineRefFixtures.LINE_BY_ID;
import static es.codeurjc.students.trainfyre.network.application.service.LineRefFixtures.LINE_ID;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.BY_SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenLookupFails;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenNoMapFound;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GetLineServiceTest {

    private static final String MAP_REFS =
            "es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs";
    private static final String LINE_REFS =
            "es.codeurjc.students.trainfyre.network.application.service.LineRefFixtures#";
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final LineName NAME = new LineName("C1", "Cercanias C1");
    private static final LineType TYPE = LineType.RAIL;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private GetLineUseCase getLineUseCase;
    private TransportMap existingMap;
    private Line existingLine;

    @BeforeEach
    void setUp() {
        getLineUseCase = new GetLineService(new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort)));
        existingLine = Line.reconstitute(LINE_ID, EXTERNAL_ID, NAME, TYPE, COLORS);
        existingMap = TransportMap.reconstitute(MapId.generate(), METADATA, CREATED_AT, List.of(existingLine));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(LINE_REFS + "mapAndLineRefs")
    void shouldReturnTheLineFoundByTheRefInTheMapFoundByTheRefWhenQueryIsValid(
            String scenario, MapRef mapRef, LineRef lineRef) {
        givenMapFound(transportMapPort, mapRef, existingMap);

        Line result = getLineUseCase.execute(new GetLineQuery(mapRef, lineRef));

        assertThat(result).isSameAs(existingLine);
        assertThat(existingMap.getLastUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldThrowMapNotFoundExceptionWhenNoMapMatchesTheRef(String scenario, MapRef mapRef) {
        givenNoMapFound(transportMapPort, mapRef);

        assertThatThrownBy(() -> getLineUseCase.execute(new GetLineQuery(mapRef, LINE_BY_ID)))
                .isInstanceOf(MapNotFoundException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldPropagateExceptionWhenTheMapLookupFails(String scenario, MapRef mapRef) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, mapRef, lookupFailure);

        assertThatThrownBy(() -> getLineUseCase.execute(new GetLineQuery(mapRef, LINE_BY_ID)))
                .isSameAs(lookupFailure);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(LINE_REFS + "unknownLineRefs")
    void shouldThrowLineNotFoundExceptionWhenTheMapHasNoLineMatchingTheRef(String scenario, LineRef lineRef) {
        givenMapFound(transportMapPort, BY_SOURCE, existingMap);

        assertThatThrownBy(() -> getLineUseCase.execute(new GetLineQuery(BY_SOURCE, lineRef)))
                .isInstanceOf(LineNotFoundException.class);
    }
}

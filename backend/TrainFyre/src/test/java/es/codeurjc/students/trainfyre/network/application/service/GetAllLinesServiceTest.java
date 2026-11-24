package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetAllLinesUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllLinesQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.HexColor;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
import es.codeurjc.students.trainfyre.network.domain.LineId;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import es.codeurjc.students.trainfyre.network.domain.MapId;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.BY_SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenLookupFails;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenNoMapFound;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GetAllLinesServiceTest {

    private static final String MAP_REFS =
            "es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs";
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final LineName NAME = new LineName("C1", "Cercanias C1");
    private static final LineType TYPE = LineType.RAIL;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private GetAllLinesUseCase getAllLinesUseCase;

    @BeforeEach
    void setUp() {
        getAllLinesUseCase = new GetAllLinesService(
                new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldReturnAllTheLinesOfTheMapFoundByTheRefWhenQueryIsValid(String scenario, MapRef mapRef) {
        Line firstLine = Line.reconstitute(new LineId(7L), "C-1", NAME, TYPE, COLORS);
        Line secondLine = Line.reconstitute(new LineId(8L), "C-2", NAME, TYPE, COLORS);
        givenMapFound(transportMapPort, mapRef, mapWith(firstLine, secondLine));

        Collection<Line> result = getAllLinesUseCase.execute(new GetAllLinesQuery(mapRef));

        assertThat(result).containsExactlyInAnyOrder(firstLine, secondLine);
    }

    @Test
    void shouldReturnNoLinesWhenTheMapHasNone() {
        givenMapFound(transportMapPort, BY_SOURCE, mapWith());

        Collection<Line> result = getAllLinesUseCase.execute(new GetAllLinesQuery(BY_SOURCE));

        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldThrowMapNotFoundExceptionWhenNoMapMatchesTheRef(String scenario, MapRef mapRef) {
        givenNoMapFound(transportMapPort, mapRef);

        assertThatThrownBy(() -> getAllLinesUseCase.execute(new GetAllLinesQuery(mapRef)))
                .isInstanceOf(MapNotFoundException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(MAP_REFS)
    void shouldPropagateExceptionWhenTheMapLookupFails(String scenario, MapRef mapRef) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, mapRef, lookupFailure);

        assertThatThrownBy(() -> getAllLinesUseCase.execute(new GetAllLinesQuery(mapRef)))
                .isSameAs(lookupFailure);
    }

    private static TransportMap mapWith(Line... lines) {
        return TransportMap.reconstitute(MapId.generate(), METADATA, CREATED_AT, List.of(lines));
    }
}

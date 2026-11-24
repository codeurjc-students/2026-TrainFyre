package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
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

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenLookupFails;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenNoMapFound;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GetTransportMapServiceTest {

    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private GetTransportMapUseCase getTransportMapUseCase;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        getTransportMapUseCase = new GetTransportMapService(
                new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort)));
        existingMap = TransportMap.createTransportMap(METADATA, CREATED_AT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldReturnTheMapFoundByTheRefWhenQueryIsValid(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        TransportMap result = getTransportMapUseCase.execute(new GetTransportMapQuery(ref));

        assertThat(result).isSameAs(existingMap);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldThrowMapNotFoundExceptionWhenNoMapMatchesTheRef(String scenario, MapRef ref) {
        givenNoMapFound(transportMapPort, ref);

        assertThatThrownBy(() -> getTransportMapUseCase.execute(new GetTransportMapQuery(ref)))
                .isInstanceOf(MapNotFoundException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateExceptionWhenTheLookupFails(String scenario, MapRef ref) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, ref, lookupFailure);

        assertThatThrownBy(() -> getTransportMapUseCase.execute(new GetTransportMapQuery(ref)))
                .isSameAs(lookupFailure);
    }
}

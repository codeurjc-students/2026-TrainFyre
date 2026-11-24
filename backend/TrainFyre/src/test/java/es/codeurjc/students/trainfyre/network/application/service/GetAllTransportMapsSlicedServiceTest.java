package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.SliceResponse;
import es.codeurjc.students.trainfyre.network.application.port.in.GetAllTransportMapsSlicedUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllTransportMapsSlicedQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllTransportMapsSlicedServiceTest {

    private static final Pageable PAGEABLE = new Pageable(1, 2);
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private GetAllTransportMapsSlicedUseCase getAllTransportMapsSlicedUseCase;

    @BeforeEach
    void setUp() {
        getAllTransportMapsSlicedUseCase = new GetAllTransportMapsSlicedService(transportMapPort);
    }

    static Stream<Arguments> slices() {
        List<TransportMap> maps = List.of(
                TransportMap.createTransportMap(METADATA, CREATED_AT),
                TransportMap.createTransportMap(METADATA, CREATED_AT));
        return Stream.of(
                Arguments.of("slice with a next one", new SliceResponse<>(maps, 1, true)),
                Arguments.of("last slice", new SliceResponse<>(maps, 1, false)),
                Arguments.of("empty slice", new SliceResponse<TransportMap>(List.of(), 1, false))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("slices")
    void shouldReturnTheSliceFoundByThePortForThePageableWhenQueryIsValid(
            String scenario, SliceResponse<TransportMap> slice) {
        when(transportMapPort.findSlice(PAGEABLE)).thenReturn(slice);

        SliceResponse<TransportMap> result = getAllTransportMapsSlicedUseCase
                .execute(new GetAllTransportMapsSlicedQuery(PAGEABLE));

        assertThat(result).isSameAs(slice);
    }

    @Test
    void shouldPropagateExceptionWhenTheSliceLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not read transport maps");
        when(transportMapPort.findSlice(PAGEABLE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> getAllTransportMapsSlicedUseCase
                .execute(new GetAllTransportMapsSlicedQuery(PAGEABLE)))
                .isSameAs(lookupFailure);
    }
}

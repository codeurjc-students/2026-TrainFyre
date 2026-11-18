package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportMapTest {

    private static final MapMetadata METADATA =
            new MapMetadata("Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);

    @Test
    void shouldThrowNullPointerExceptionWhenMetadataIsNull() {
        assertThatThrownBy(() -> TransportMap.createTransportMap(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldGenerateRandomIdWhenMapIsCreated() {
        TransportMap first = TransportMap.createTransportMap(METADATA);
        TransportMap second = TransportMap.createTransportMap(METADATA);

        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void shouldHaveNullLastUpdatedAtWhenMapIsCreated() {
        assertThat(TransportMap.createTransportMap(METADATA).getLastUpdatedAt()).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineViews")
    void shouldHaveNoLinesWhenMapIsCreated(String view, Function<TransportMap, Collection<Line>> lineView) {
        assertThat(lineView.apply(TransportMap.createTransportMap(METADATA))).isNotNull().isEmpty();
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("lineViewModifications")
    void shouldThrowUnsupportedOperationExceptionWhenLineViewIsModified(String view,
                                                                        String operation,
                                                                        Function<TransportMap, Collection<Line>> lineView,
                                                                        Consumer<Collection<Line>> modification) {
        Collection<Line> lines = lineView.apply(TransportMap.createTransportMap(METADATA));

        assertThatThrownBy(() -> modification.accept(lines))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> lineViews() {
        return Stream.of(
                Arguments.of("lines", (Function<TransportMap, Collection<Line>>) TransportMap::lines),
                Arguments.of("linesByExternalId", (Function<TransportMap, Collection<Line>>) TransportMap::linesByExternalId));
    }

    private static Stream<Arguments> lineViewModifications() {
        Line line = Line.createLine("C1", new LineName("C1", "Cercanias C1"), LineType.RAIL, null);
        Map<String, Consumer<Collection<Line>>> modifications = Map.of(
                "add", lines -> lines.add(line),
                "remove", lines -> lines.remove(line),
                "clear", Collection::clear);

        return lineViews().flatMap(view -> modifications.entrySet().stream()
                .map(modification -> Arguments.of(view.get()[0], modification.getKey(),
                        view.get()[1], modification.getValue())));
    }
}

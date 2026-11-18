package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.util.Collection;
import java.util.function.Consumer;
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

    @Test
    void shouldHaveNoLinesWhenMapIsCreated() {
        assertThat(TransportMap.createTransportMap(METADATA).lines()).isNotNull().isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("linesModifications")
    void shouldThrowUnsupportedOperationExceptionWhenLinesViewIsModified(String operation,
                                                                         Consumer<Collection<Line>> modification) {
        Collection<Line> lines = TransportMap.createTransportMap(METADATA).lines();

        assertThatThrownBy(() -> modification.accept(lines))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> linesModifications() {
        Line line = Line.createLine("C1", new LineName("C1", "Cercanias C1"), LineType.RAIL, null);
        return Stream.of(
                Arguments.of("add", (Consumer<Collection<Line>>) lines -> lines.add(line)),
                Arguments.of("remove", (Consumer<Collection<Line>>) lines -> lines.remove(line)),
                Arguments.of("clear", (Consumer<Collection<Line>>) Collection::clear));
    }
}

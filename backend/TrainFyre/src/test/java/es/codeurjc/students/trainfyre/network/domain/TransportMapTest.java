package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportMapTest {

    private static final MapMetadata METADATA =
            new MapMetadata("Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Line LINE_WITHOUT_ID = newLine(null, "C1");
    private static final Line LINE_WITH_ID = newLine(1L, "C2");

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArgumentsForCreateWithLines")
    void shouldThrowNullPointerExceptionWhenMapIsCreatedFromLinesWithNullArguments(String scenario,
                                                                                   MapMetadata metadata,
                                                                                   Collection<Line> lines) {
        assertThatThrownBy(() -> TransportMap.createTransportMap(metadata, lines))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldInitializeIdAndLastUpdatedAtWhenMapIsCreatedFromLines() {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID));

        assertThat(map.getId()).isNotNull();
        assertThat(map.getLastUpdatedAt()).isNull();
    }

    @Test
    void shouldIndexEveryLineByExternalIdWhenMapIsCreatedFromLines() {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID, LINE_WITH_ID));

        assertThat(map.linesByExternalId()).containsExactlyInAnyOrder(LINE_WITHOUT_ID, LINE_WITH_ID);
    }

    @Test
    void shouldIndexOnlyLinesWithIdByLineIdWhenMapIsCreatedFromLines() {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID, LINE_WITH_ID));

        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineViews")
    void shouldHaveNoLinesWhenMapIsCreatedFromEmptyLines(String view, Function<TransportMap, Collection<Line>> lineView) {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of());

        assertThat(lineView.apply(map)).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicatedLines")
    void shouldThrowDuplicateLineExceptionWhenMapIsCreatedFromDuplicatedLines(String scenario, List<Line> lines) {
        assertThatThrownBy(() -> TransportMap.createTransportMap(METADATA, lines))
                .isInstanceOf(DuplicateLineException.class);
    }

    @Test
    void shouldNotChangeMapWhenSourceLinesAreModifiedAfterCreation() {
        List<Line> sourceLines = new ArrayList<>(List.of(LINE_WITH_ID));
        TransportMap map = TransportMap.createTransportMap(METADATA, sourceLines);

        sourceLines.add(LINE_WITHOUT_ID);
        sourceLines.remove(LINE_WITH_ID);

        assertThat(map.linesByExternalId()).containsExactly(LINE_WITH_ID);
        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
    }

    private static Stream<Arguments> invalidNullArgumentsForCreateWithLines() {
        return Stream.of(
                Arguments.of("null metadata", null, List.of(LINE_WITHOUT_ID)),
                Arguments.of("null lines", METADATA, null),
                Arguments.of("null line inside lines", METADATA, Arrays.asList(LINE_WITHOUT_ID, null)));
    }

    private static Stream<Arguments> duplicatedLines() {
        return Stream.of(
                Arguments.of("same external id", List.of(newLine(null, "C1"), newLine(null, "C1"))),
                Arguments.of("same external id with ids", List.of(newLine(1L, "C1"), newLine(2L, "C1"))),
                Arguments.of("same line id", List.of(newLine(1L, "C1"), newLine(1L, "C2"))));
    }

    private static Line newLine(Long id, String externalId) {
        LineName name = new LineName(externalId, "Cercanias " + externalId);
        if (id == null) {
            return Line.createLine(externalId, name, LineType.RAIL, null);
        }
        return Line.reconstitute(new LineId(id), externalId, name, LineType.RAIL, LineColors.defaultFor(externalId));
    }
}

package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class TransportMapTest {

    private static final MapMetadata METADATA =
            new MapMetadata("Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final MapId MAP_ID = new MapId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    private static final MapId OTHER_MAP_ID = new MapId(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"));
    private static final MapMetadata OTHER_METADATA =
            new MapMetadata("Metro Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata FULL_METADATA =
            new MapMetadata("Cercanias Madrid", ZoneId.of("Europe/Madrid"), "ES", URI.create("https://www.renfe.com"));
    private static final Instant LATER = NOW.plusSeconds(3600);
    private static final Instant EARLIER = NOW.minusSeconds(3600);
    private static final Line LINE_WITHOUT_ID =newLine(null, "C1");
    private static final Line LINE_WITH_ID = newLine(1L, "C2");
    private static final Line SECOND_LINE_WITH_ID = newLine(2L, "C3");

    @Test
    void shouldThrowNullPointerExceptionWhenMetadataIsNull() {
        assertThatThrownBy(() -> TransportMap.createTransportMap(null, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldGenerateRandomIdWhenMapIsCreated() {
        TransportMap first = TransportMap.createTransportMap(METADATA, NOW);
        TransportMap second = TransportMap.createTransportMap(METADATA, NOW);

        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenNowIsNull() {
        assertThatThrownBy(() -> TransportMap.createTransportMap(METADATA, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldSetLastUpdatedAtToNowWhenMapIsCreated() {
        assertThat(TransportMap.createTransportMap(METADATA, NOW).getLastUpdatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineViews")
    void shouldHaveNoLinesWhenMapIsCreated(String view, Function<TransportMap, Collection<Line>> lineView) {
        assertThat(lineView.apply(TransportMap.createTransportMap(METADATA, NOW))).isNotNull().isEmpty();
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("lineViewModifications")
    void shouldThrowUnsupportedOperationExceptionWhenLineViewIsModified(String view,
                                                                        String operation,
                                                                        Function<TransportMap, Collection<Line>> lineView,
                                                                        Consumer<Collection<Line>> modification) {
        Collection<Line> lines = lineView.apply(TransportMap.createTransportMap(METADATA, NOW));

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
                                                                                   Collection<Line> lines,
                                                                                   Instant now) {
        assertThatThrownBy(() -> TransportMap.createTransportMap(metadata, lines, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldInitializeIdAndLastUpdatedAtToNowWhenMapIsCreatedFromLines() {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID), NOW);

        assertThat(map.getId()).isNotNull();
        assertThat(map.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldIndexEveryLineByExternalIdWhenMapIsCreatedFromLines() {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID, LINE_WITH_ID), NOW);

        assertThat(map.linesByExternalId()).containsExactlyInAnyOrder(LINE_WITHOUT_ID, LINE_WITH_ID);
    }

    @Test
    void shouldIndexOnlyLinesWithIdByLineIdWhenMapIsCreatedFromLines() {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID, LINE_WITH_ID), NOW);

        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineViews")
    void shouldHaveNoLinesWhenMapIsCreatedFromEmptyLines(String view, Function<TransportMap, Collection<Line>> lineView) {
        TransportMap map = TransportMap.createTransportMap(METADATA, List.of(), NOW);

        assertThat(lineView.apply(map)).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicatedLines")
    void shouldThrowDuplicateLineExceptionWhenMapIsCreatedFromDuplicatedLines(String scenario, List<Line> lines) {
        assertThatThrownBy(() -> TransportMap.createTransportMap(METADATA, lines, NOW))
                .isInstanceOf(DuplicateLineException.class);
    }

    @Test
    void shouldNotChangeMapWhenSourceLinesAreModifiedAfterCreation() {
        List<Line> sourceLines = new ArrayList<>(List.of(LINE_WITH_ID));
        TransportMap map = TransportMap.createTransportMap(METADATA, sourceLines, NOW);

        sourceLines.add(LINE_WITHOUT_ID);
        sourceLines.remove(LINE_WITH_ID);

        assertThat(map.linesByExternalId()).containsExactly(LINE_WITH_ID);
        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArgumentsForReconstitute")
    void shouldThrowNullPointerExceptionWhenMapIsReconstitutedWithNullArguments(String scenario,
                                                                                MapId id,
                                                                                MapMetadata metadata,
                                                                                Instant lastUpdatedAt,
                                                                                Collection<Line> lines) {
        assertThatThrownBy(() -> TransportMap.reconstitute(id, metadata, lastUpdatedAt, lines))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldKeepIdMetadataAndLastUpdatedAtWhenMapIsReconstituted() {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));

        assertThat(map.getId()).isEqualTo(MAP_ID);
        assertThat(map.getMapMetadata()).isEqualTo(METADATA);
        assertThat(map.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineViews")
    void shouldIndexEveryLineWhenMapIsReconstituted(String view, Function<TransportMap, Collection<Line>> lineView) {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID, SECOND_LINE_WITH_ID));

        assertThat(lineView.apply(map)).containsExactlyInAnyOrder(LINE_WITH_ID, SECOND_LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineViews")
    void shouldHaveNoLinesWhenMapIsReconstitutedFromEmptyLines(String view, Function<TransportMap, Collection<Line>> lineView) {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of());

        assertThat(lineView.apply(map)).isEmpty();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenReconstitutedLineHasNoId() {
        assertThatThrownBy(() -> TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITHOUT_ID)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicatedPersistedLines")
    void shouldThrowDuplicateLineExceptionWhenMapIsReconstitutedFromDuplicatedLines(String scenario, List<Line> lines) {
        assertThatThrownBy(() -> TransportMap.reconstitute(MAP_ID, METADATA, NOW, lines))
                .isInstanceOf(DuplicateLineException.class);
    }

    @Test
    void shouldNotChangeReconstitutedMapWhenSourceLinesAreModifiedAfterReconstitution() {
        List<Line> sourceLines = new ArrayList<>(List.of(LINE_WITH_ID));
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, sourceLines);

        sourceLines.add(SECOND_LINE_WITH_ID);
        sourceLines.remove(LINE_WITH_ID);

        assertThat(map.linesByExternalId()).containsExactly(LINE_WITH_ID);
        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mapsWithSameIdAndDifferentState")
    void shouldBeEqualWhenMapsHaveSameId(String scenario, TransportMap other) {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));

        assertThat(map).isEqualTo(other);
    }

    @Test
    void shouldNotBeEqualWhenMapsHaveDifferentIdAndSameState() {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));
        TransportMap other = TransportMap.reconstitute(OTHER_MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));

        assertThat(map).isNotEqualTo(other);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mapsWithSameIdAndDifferentState")
    void shouldHaveSameHashCodeWhenMapsHaveSameId(String scenario, TransportMap other) {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));

        assertThat(map).hasSameHashCodeAs(other);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("objectsThatAreNotMaps")
    void shouldNotBeEqualWhenComparedWithNullOrAnotherType(String scenario, Object other) {
        TransportMap map = TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));

        assertThat(map).isNotEqualTo(other);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArgumentsForUpdateMetadata")
    void shouldThrowNullPointerExceptionWhenMetadataIsUpdatedWithNullArguments(String scenario,
                                                                               MapMetadata metadata,
                                                                               Instant now) {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.updateMetadata(metadata, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenMetadataIsUpdatedWithInstantBeforeLastUpdate() {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.updateMetadata(OTHER_METADATA, EARLIER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedMetadataUpdates")
    void shouldKeepMetadataAndLastUpdatedAtWhenMetadataUpdateIsRejected(String scenario,
                                                                        MapMetadata metadata,
                                                                        Instant now) {
        TransportMap map = mapUpdatedAtNow();

        catchThrowable(() -> map.updateMetadata(metadata, now));

        assertThat(map.getMapMetadata()).isEqualTo(METADATA);
        assertThat(map.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldReplaceWholeMetadataWhenMetadataIsUpdated() {
        TransportMap map = TransportMap.reconstitute(MAP_ID, FULL_METADATA, NOW, List.of());

        map.updateMetadata(METADATA, LATER);

        assertThat(map.getMapMetadata()).isEqualTo(METADATA);
        assertThat(map.getMapMetadata().country()).isNull();
        assertThat(map.getMapMetadata().website()).isNull();
    }

    @Test
    void shouldSetLastUpdatedAtToNowWhenMetadataIsUpdated() {
        TransportMap map = mapUpdatedAtNow();

        map.updateMetadata(OTHER_METADATA, LATER);

        assertThat(map.getLastUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    void shouldAcceptInstantEqualToLastUpdateWhenMetadataIsUpdated() {
        TransportMap map = mapUpdatedAtNow();

        assertThatCode(() -> map.updateMetadata(OTHER_METADATA, NOW)).doesNotThrowAnyException();
        assertThat(map.getMapMetadata()).isEqualTo(OTHER_METADATA);
    }

    @Test
    void shouldSetLastUpdatedAtToNowWhenUpdatedMetadataIsEqualToCurrent() {
        TransportMap map = mapUpdatedAtNow();

        map.updateMetadata(METADATA, LATER);

        assertThat(map.getLastUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    void shouldKeepIdLinesAndEqualityWhenMetadataIsUpdated() {
        TransportMap map = mapUpdatedAtNow();
        TransportMap sameIdBeforeUpdate = mapUpdatedAtNow();
        int hashCodeBeforeUpdate = map.hashCode();

        map.updateMetadata(OTHER_METADATA, LATER);

        assertThat(map.getId()).isEqualTo(MAP_ID);
        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
        assertThat(map.linesByExternalId()).containsExactly(LINE_WITH_ID);
        assertThat(map).isEqualTo(sameIdBeforeUpdate);
        assertThat(map.hashCode()).isEqualTo(hashCodeBeforeUpdate);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenNullLineIsAdded() {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.addLine(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldIndexOnlyByExternalIdWhenLineWithoutIdIsAdded() {
        TransportMap map = mapUpdatedAtNow();

        map.addLine(LINE_WITHOUT_ID);

        assertThat(map.linesByExternalId()).containsExactlyInAnyOrder(LINE_WITH_ID, LINE_WITHOUT_ID);
        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
    }

    @Test
    void shouldIndexByExternalIdAndLineIdWhenLineWithIdIsAdded() {
        TransportMap map = mapUpdatedAtNow();

        map.addLine(SECOND_LINE_WITH_ID);

        assertThat(map.linesByExternalId()).containsExactlyInAnyOrder(LINE_WITH_ID, SECOND_LINE_WITH_ID);
        assertThat(map.lines()).containsExactlyInAnyOrder(LINE_WITH_ID, SECOND_LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicatedLinesToAdd")
    void shouldThrowDuplicateLineExceptionWhenDuplicatedLineIsAdded(String scenario, Line line) {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.addLine(line))
                .isInstanceOf(DuplicateLineException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedLinesToAdd")
    void shouldKeepLinesAndLastUpdatedAtWhenLineIsRejected(String scenario, Line line) {
        TransportMap map = mapUpdatedAtNow();

        catchThrowable(() -> map.addLine(line));

        assertMapUnchanged(map);
    }

    @Test
    void shouldKeepLastUpdatedAtWhenLineIsAddedWithoutInstant() {
        TransportMap map = mapUpdatedAtNow();

        map.addLine(LINE_WITHOUT_ID);

        assertThat(map.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldKeepIdAndEqualityWhenLineIsAdded() {
        TransportMap map = mapUpdatedAtNow();
        TransportMap sameIdBeforeAdd = mapUpdatedAtNow();
        int hashCodeBeforeAdd = map.hashCode();

        map.addLine(LINE_WITHOUT_ID, LATER);

        assertThat(map.getId()).isEqualTo(MAP_ID);
        assertThat(map).isEqualTo(sameIdBeforeAdd);
        assertThat(map.hashCode()).isEqualTo(hashCodeBeforeAdd);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArgumentsForAddLineWithInstant")
    void shouldThrowNullPointerExceptionWhenLineIsAddedWithNullArguments(String scenario, Line line, Instant now) {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.addLine(line, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenLineIsAddedWithInstantBeforeLastUpdate() {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.addLine(LINE_WITHOUT_ID, EARLIER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicatedLinesToAdd")
    void shouldThrowDuplicateLineExceptionWhenDuplicatedLineIsAddedWithInstant(String scenario, Line line) {
        TransportMap map = mapUpdatedAtNow();

        assertThatThrownBy(() -> map.addLine(line, LATER))
                .isInstanceOf(DuplicateLineException.class);
    }

    @Test
    void shouldIndexLineWhenLineIsAddedWithInstant() {
        TransportMap map = mapUpdatedAtNow();

        map.addLine(LINE_WITHOUT_ID, LATER);
        map.addLine(SECOND_LINE_WITH_ID, LATER);

        assertThat(map.linesByExternalId()).containsExactlyInAnyOrder(LINE_WITH_ID, LINE_WITHOUT_ID, SECOND_LINE_WITH_ID);
        assertThat(map.lines()).containsExactlyInAnyOrder(LINE_WITH_ID, SECOND_LINE_WITH_ID);
    }

    @Test
    void shouldSetLastUpdatedAtToNowWhenLineIsAddedWithInstant() {
        TransportMap map = mapUpdatedAtNow();

        map.addLine(LINE_WITHOUT_ID, LATER);

        assertThat(map.getLastUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    void shouldAcceptInstantEqualToLastUpdateWhenLineIsAddedWithInstant() {
        TransportMap map = mapUpdatedAtNow();

        assertThatCode(() -> map.addLine(LINE_WITHOUT_ID, NOW)).doesNotThrowAnyException();
        assertThat(map.linesByExternalId()).contains(LINE_WITHOUT_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedLinesToAddWithInstant")
    void shouldKeepLinesAndLastUpdatedAtWhenLineWithInstantIsRejected(String scenario, Line line, Instant now) {
        TransportMap map = mapUpdatedAtNow();

        catchThrowable(() -> map.addLine(line, now));

        assertMapUnchanged(map);
    }

    private static void assertMapUnchanged(TransportMap map) {
        assertThat(map.lines()).containsExactly(LINE_WITH_ID);
        assertThat(map.linesByExternalId()).containsExactly(LINE_WITH_ID);
        assertThat(map.getLastUpdatedAt()).isEqualTo(NOW);
    }

    private static Stream<Arguments> duplicatedLinesToAdd() {
        return Stream.of(
                Arguments.of("same external id without id", newLine(null, "C2")),
                Arguments.of("same external id with id", newLine(5L, "C2")),
                Arguments.of("same line id", newLine(1L, "C9")));
    }

    private static Stream<Arguments> rejectedLinesToAdd() {
        return Stream.concat(
                Stream.of(Arguments.of("null line", (Line) null)),
                duplicatedLinesToAdd());
    }

    private static Stream<Arguments> invalidNullArgumentsForAddLineWithInstant() {
        return Stream.of(
                Arguments.of("null line", null, LATER),
                Arguments.of("null now", LINE_WITHOUT_ID, null),
                Arguments.of("null line and now", null, null));
    }

    private static Stream<Arguments> rejectedLinesToAddWithInstant() {
        return Stream.of(
                invalidNullArgumentsForAddLineWithInstant(),
                Stream.of(Arguments.of("now before last update", LINE_WITHOUT_ID, EARLIER)),
                duplicatedLinesToAdd().map(arguments -> Arguments.of(arguments.get()[0], arguments.get()[1], LATER)))
                .flatMap(Function.identity());
    }

    private static TransportMap mapUpdatedAtNow() {
        return TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID));
    }

    private static Stream<Arguments> invalidNullArgumentsForUpdateMetadata() {
        return Stream.of(
                Arguments.of("null metadata", null, LATER),
                Arguments.of("null now", OTHER_METADATA, null),
                Arguments.of("null metadata and now", null, null));
    }

    private static Stream<Arguments> rejectedMetadataUpdates() {
        return Stream.concat(
                invalidNullArgumentsForUpdateMetadata(),
                Stream.of(Arguments.of("now before last update", OTHER_METADATA, EARLIER)));
    }

    private static Stream<Arguments> mapsWithSameIdAndDifferentState() {
        return Stream.of(
                Arguments.of("same state", TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(LINE_WITH_ID))),
                Arguments.of("different metadata", TransportMap.reconstitute(MAP_ID, OTHER_METADATA, NOW, List.of(LINE_WITH_ID))),
                Arguments.of("different lastUpdatedAt", TransportMap.reconstitute(MAP_ID, METADATA, LATER, List.of(LINE_WITH_ID))),
                Arguments.of("different lines", TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(SECOND_LINE_WITH_ID))),
                Arguments.of("no lines", TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of())));
    }

    private static Stream<Arguments> objectsThatAreNotMaps() {
        return Stream.of(
                Arguments.of("null", null),
                Arguments.of("its own id", MAP_ID),
                Arguments.of("a string", "map"));
    }

    private static Stream<Arguments> invalidNullArgumentsForReconstitute() {
        List<Line> lines = List.of(LINE_WITH_ID);
        return Stream.of(
                Arguments.of("null id", null, METADATA, NOW, lines),
                Arguments.of("null metadata", MAP_ID, null, NOW, lines),
                Arguments.of("null lastUpdatedAt", MAP_ID, METADATA, null, lines),
                Arguments.of("null lines", MAP_ID, METADATA, NOW, null),
                Arguments.of("null line inside lines", MAP_ID, METADATA, NOW, Arrays.asList(LINE_WITH_ID, null)));
    }

    private static Stream<Arguments> duplicatedPersistedLines() {
        return Stream.of(
                Arguments.of("same external id", List.of(newLine(1L, "C1"), newLine(2L, "C1"))),
                Arguments.of("same line id", List.of(newLine(1L, "C1"), newLine(1L, "C2"))));
    }

    private static Stream<Arguments> invalidNullArgumentsForCreateWithLines() {
        return Stream.of(
                Arguments.of("null metadata", null, List.of(LINE_WITHOUT_ID), NOW),
                Arguments.of("null lines", METADATA, null, NOW),
                Arguments.of("null line inside lines", METADATA, Arrays.asList(LINE_WITHOUT_ID, null), NOW),
                Arguments.of("null now", METADATA, List.of(LINE_WITHOUT_ID), null));
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

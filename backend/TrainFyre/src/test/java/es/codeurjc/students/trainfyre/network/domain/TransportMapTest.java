package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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
    private static final LineId UPDATABLE_LINE_ID = new LineId(1L);
    private static final LineId OTHER_LINE_ID = new LineId(2L);
    private static final String UPDATABLE_EXTERNAL_ID = "C2";
    private static final LineName NEW_LINE_NAME = new LineName("N1", "New line");
    private static final LineType NEW_LINE_TYPE = LineType.METRO;
    private static final LineColors NEW_LINE_COLORS = new LineColors(new HexColor("00FF00"), new HexColor("000000"));
    private static final LineChanges LINE_CHANGES = new LineChanges(NEW_LINE_NAME, NEW_LINE_TYPE, NEW_LINE_COLORS);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("nullLineSearches")
    void shouldThrowNullPointerExceptionWhenLineIsFoundWithNullArgument(String scenario,
                                                                        Function<TransportMap, Line> search) {
        TransportMap map = mapWithLinesWithAndWithoutId();

        assertThatThrownBy(() -> search.apply(map))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "[{index}]")
    @ValueSource(strings = {"", " ", "\t\n"})
    void shouldThrowIllegalArgumentExceptionWhenLineIsFoundWithEmptyOrBlankExternalId(String externalId) {
        TransportMap map = mapWithLinesWithAndWithoutId();

        assertThatThrownBy(() -> map.findLine(externalId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnSameLineWhenLineIsFoundByLineId() {
        TransportMap map = mapWithLinesWithAndWithoutId();

        assertThat(map.findLine(LINE_WITH_ID.getId())).isSameAs(LINE_WITH_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("existingLinesByExternalId")
    void shouldReturnSameLineWhenLineIsFoundByExternalId(String scenario, String externalId, Line expected) {
        TransportMap map = mapWithLinesWithAndWithoutId();

        assertThat(map.findLine(externalId)).isSameAs(expected);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingLineSearches")
    void shouldThrowLineNotFoundExceptionWhenLineDoesNotExist(String scenario, Function<TransportMap, Line> search) {
        TransportMap map = mapWithLinesWithAndWithoutId();

        assertThatThrownBy(() -> search.apply(map))
                .isInstanceOf(LineNotFoundException.class);
    }

    @Test
    void shouldNotChangeMapWhenLineIsFound() {
        TransportMap map = mapUpdatedAtNow();

        map.findLine(LINE_WITH_ID.getId());
        map.findLine(LINE_WITH_ID.getExternalId());

        assertMapUnchanged(map);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArgumentsForUpdateLine")
    void shouldThrowNullPointerExceptionWhenLineIsUpdatedWithNullArguments(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        assertThatThrownBy(() -> update.apply(map, LINE_CHANGES, LATER))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "[{index}]")
    @ValueSource(strings = {"", " ", "\t\n"})
    void shouldThrowIllegalArgumentExceptionWhenLineIsUpdatedWithEmptyOrBlankExternalId(String externalId) {
        TransportMap map = mapWithUpdatableLines();

        assertThatThrownBy(() -> map.updateLine(externalId, LINE_CHANGES, LATER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineUpdates")
    void shouldThrowIllegalArgumentExceptionWhenLineIsUpdatedWithInstantBeforeLastUpdate(String scenario,
                                                                                         LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        assertThatThrownBy(() -> update.apply(map, LINE_CHANGES, EARLIER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unknownLineUpdates")
    void shouldThrowLineNotFoundExceptionWhenUpdatedLineDoesNotExist(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        assertThatThrownBy(() -> update.apply(map, LINE_CHANGES, LATER))
                .isInstanceOf(LineNotFoundException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineUpdates")
    void shouldApplyChangesToTheSameLineWhenLineIsUpdated(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();
        Line line = map.findLine(UPDATABLE_LINE_ID);

        update.apply(map, LINE_CHANGES, LATER);

        assertThat(map.findLine(UPDATABLE_LINE_ID)).isSameAs(line);
        assertThat(line.getName()).isEqualTo(NEW_LINE_NAME);
        assertThat(line.getType()).isEqualTo(NEW_LINE_TYPE);
        assertThat(line.getColors()).isEqualTo(NEW_LINE_COLORS);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineUpdates")
    void shouldKeepIdentityAndIndexesWhenLineIsUpdated(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();
        Line line = map.findLine(UPDATABLE_LINE_ID);

        update.apply(map, LINE_CHANGES, LATER);

        assertThat(line.getId()).isEqualTo(UPDATABLE_LINE_ID);
        assertThat(line.getExternalId()).isEqualTo(UPDATABLE_EXTERNAL_ID);
        assertThat(map.findLine(UPDATABLE_EXTERNAL_ID)).isSameAs(line);
        assertThat(map.lines()).hasSize(2);
        assertThat(map.linesByExternalId()).hasSize(2);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineUpdates")
    void shouldNotChangeOtherLinesWhenLineIsUpdated(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        update.apply(map, LINE_CHANGES, LATER);

        Line other = map.findLine(OTHER_LINE_ID);
        assertThat(other.getName()).isEqualTo(newLine(2L, "C3").getName());
        assertThat(other.getType()).isEqualTo(newLine(2L, "C3").getType());
        assertThat(other.getColors()).isEqualTo(newLine(2L, "C3").getColors());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineUpdates")
    void shouldSetLastUpdatedAtToNowWhenLineIsUpdated(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        update.apply(map, LINE_CHANGES, LATER);

        assertThat(map.getLastUpdatedAt()).isEqualTo(LATER);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("lineUpdates")
    void shouldAcceptInstantEqualToLastUpdateWhenLineIsUpdated(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        assertThatCode(() -> update.apply(map, LINE_CHANGES, NOW)).doesNotThrowAnyException();
        assertThat(map.findLine(UPDATABLE_LINE_ID).getName()).isEqualTo(NEW_LINE_NAME);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedLineUpdates")
    void shouldKeepLineAndLastUpdatedAtWhenLineUpdateIsRejected(String scenario, LineUpdate update) {
        TransportMap map = mapWithUpdatableLines();

        catchThrowable(() -> update.apply(map, LINE_CHANGES, LATER));

        Line line = map.findLine(UPDATABLE_LINE_ID);
        assertThat(line.getName()).isEqualTo(newLine(1L, "C2").getName());
        assertThat(line.getType()).isEqualTo(newLine(1L, "C2").getType());
        assertThat(line.getColors()).isEqualTo(newLine(1L, "C2").getColors());
        assertThat(map.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @FunctionalInterface
    private interface LineUpdate {
        void apply(TransportMap map, LineChanges changes, Instant now);
    }

    private static TransportMap mapWithUpdatableLines() {
        return TransportMap.reconstitute(MAP_ID, METADATA, NOW, List.of(newLine(1L, "C2"), newLine(2L, "C3")));
    }

    private static Stream<Arguments> lineUpdates() {
        return Stream.of(
                Arguments.of("by line id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(UPDATABLE_LINE_ID, changes, now)),
                Arguments.of("by external id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(UPDATABLE_EXTERNAL_ID, changes, now)));
    }

    private static Stream<Arguments> invalidNullArgumentsForUpdateLine() {
        return Stream.of(
                Arguments.of("null line id", (LineUpdate) (map, changes, now) ->
                        map.updateLine((LineId) null, changes, now)),
                Arguments.of("null external id", (LineUpdate) (map, changes, now) ->
                        map.updateLine((String) null, changes, now)),
                Arguments.of("null changes by line id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(UPDATABLE_LINE_ID, null, now)),
                Arguments.of("null changes by external id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(UPDATABLE_EXTERNAL_ID, null, now)),
                Arguments.of("null now by line id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(UPDATABLE_LINE_ID, changes, null)),
                Arguments.of("null now by external id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(UPDATABLE_EXTERNAL_ID, changes, null)));
    }

    private static Stream<Arguments> unknownLineUpdates() {
        return Stream.of(
                Arguments.of("unknown line id", (LineUpdate) (map, changes, now) ->
                        map.updateLine(new LineId(99L), changes, now)),
                Arguments.of("unknown external id", (LineUpdate) (map, changes, now) ->
                        map.updateLine("C99", changes, now)),
                Arguments.of("external id with different case", (LineUpdate) (map, changes, now) ->
                        map.updateLine("c2", changes, now)));
    }

    private static Stream<Arguments> rejectedLineUpdates() {
        return Stream.of(
                invalidNullArgumentsForUpdateLine(),
                unknownLineUpdates(),
                Stream.of(
                        Arguments.of("now before last update by line id", (LineUpdate) (map, changes, now) ->
                                map.updateLine(UPDATABLE_LINE_ID, changes, EARLIER)),
                        Arguments.of("now before last update by external id", (LineUpdate) (map, changes, now) ->
                                map.updateLine(UPDATABLE_EXTERNAL_ID, changes, EARLIER))))
                .flatMap(Function.identity());
    }

    private static TransportMap mapWithLinesWithAndWithoutId() {
        return TransportMap.createTransportMap(METADATA, List.of(LINE_WITHOUT_ID, LINE_WITH_ID), NOW);
    }

    private static Stream<Arguments> nullLineSearches() {
        return Stream.of(
                Arguments.of("null line id", (Function<TransportMap, Line>) map -> map.findLine((LineId) null)),
                Arguments.of("null external id", (Function<TransportMap, Line>) map -> map.findLine((String) null)));
    }

    private static Stream<Arguments> existingLinesByExternalId() {
        return Stream.of(
                Arguments.of("line with id", "C2", LINE_WITH_ID),
                Arguments.of("line without id", "C1", LINE_WITHOUT_ID),
                Arguments.of("external id with surrounding blanks", " \tC2\n ", LINE_WITH_ID));
    }

    private static Stream<Arguments> missingLineSearches() {
        return Stream.of(
                Arguments.of("unknown line id", (Function<TransportMap, Line>) map -> map.findLine(new LineId(99L))),
                Arguments.of("unknown external id", (Function<TransportMap, Line>) map -> map.findLine("C99")),
                Arguments.of("external id with different case", (Function<TransportMap, Line>) map -> map.findLine("c2")));
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

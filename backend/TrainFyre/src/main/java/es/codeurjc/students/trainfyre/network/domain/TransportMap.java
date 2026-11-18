package es.codeurjc.students.trainfyre.network.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate root of a transport map and the {@link Line}s that belong to it. Two maps are equal when they
 * have the same {@link MapId}.
 * <p>
 * A line has two identities: its {@link LineId}, which is absent until the line is persisted, and its
 * external id, which is always present. Both are unique within the map, and the map indexes its lines by
 * each of them. Lines can only be modified through this class.
 * <p>
 * The operations on an existing map that receive an {@code Instant now} reject one earlier than
 * {@code lastUpdatedAt}, accept one equal to it, and set it as the new {@code lastUpdatedAt} when they
 * succeed. An operation that is rejected leaves the map unchanged. Instances are not thread-safe.
 */
@AggregateRoot
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransportMap {

    @Identity
    @Getter
    @EqualsAndHashCode.Include
    private final MapId id;
    @Getter
    private MapMetadata mapMetadata;
    @Getter
    private Instant lastUpdatedAt;
    private final Map<LineId, Line> lines = new HashMap<>();
    private final Map<String, Line> linesByExternalId = new HashMap<>();

    private TransportMap(MapId id, MapMetadata mapMetadata, Instant lastUpdatedAt) {
        this.id = id;
        this.mapMetadata = mapMetadata;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * Creates a map without lines and with a generated {@link MapId}. Its {@code lastUpdatedAt} is {@code now}.
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    public static TransportMap createTransportMap(MapMetadata mapMetadata, Instant now) {
        return createTransportMap(mapMetadata, List.of(), now);
    }

    /**
     * Creates a map with a generated {@link MapId} and the given lines, which do not need an id yet.
     * The collection is copied: changing it afterwards does not change the map. The {@code lastUpdatedAt}
     * of the map is {@code now}.
     *
     * @throws NullPointerException if any argument, or any line, is {@code null}
     * @throws DuplicateLineException if two lines have the same external id or the same {@link LineId}
     */
    public static TransportMap createTransportMap(MapMetadata mapMetadata, Collection<Line> lines, Instant now) {
        return build(MapId.generate(), mapMetadata, now, lines);
    }

    /**
     * Rebuilds a map that was already persisted, so the id and the last update are given and every line
     * must already have a {@link LineId}. The collection is copied.
     *
     * @throws NullPointerException if any argument, or any line, is {@code null}
     * @throws IllegalArgumentException if any line has no id
     * @throws DuplicateLineException if two lines have the same external id or the same {@link LineId}
     */
    public static TransportMap reconstitute(MapId id, MapMetadata mapMetadata, Instant lastUpdatedAt, Collection<Line> lines) {
        Objects.requireNonNull(lines, "lines should not be null");
        lines.forEach(TransportMap::requirePersisted);

        return build(id, mapMetadata, lastUpdatedAt, lines);
    }

    /**
     * Lines of the map that have a {@link LineId}. It is an unmodifiable view that reflects later changes
     * of the map.
     */
    public Collection<Line> lines() {
        return Collections.unmodifiableCollection(lines.values());
    }

    /**
     * All the lines of the map, including the ones without a {@link LineId}. It is an unmodifiable view
     * that reflects later changes of the map.
     */
    public Collection<Line> linesByExternalId() {
        return Collections.unmodifiableCollection(linesByExternalId.values());
    }

    /**
     * Finds a line by its {@link LineId}. A line that has no id can only be found by its external id.
     *
     * @throws NullPointerException if {@code lineId} is {@code null}
     * @throws LineNotFoundException if the map has no line with that id
     */
    public Line findLine(LineId lineId) {
        Objects.requireNonNull(lineId, "line id should not be null");

        Line line = lines.get(lineId);
        if (line == null) {
            throw new LineNotFoundException("no line with id " + lineId.value() + " exists");
        }
        return line;
    }

    /**
     * Finds a line by its external id. The external id is stripped but it is case sensitive.
     *
     * @throws NullPointerException if {@code externalId} is {@code null}
     * @throws IllegalArgumentException if {@code externalId} is empty or blank
     * @throws LineNotFoundException if the map has no line with that external id
     */
    public Line findLine(String externalId) {
        String normalizedExternalId = Line.normalizeExternalId(externalId);

        Line line = linesByExternalId.get(normalizedExternalId);
        if (line == null) {
            throw new LineNotFoundException("no line with external id " + normalizedExternalId + " exists");
        }
        return line;
    }

    /**
     * Replaces the whole {@link MapMetadata} of the map, so an optional value that is {@code null} in the new
     * metadata is cleared. The {@code source} can be replaced too.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code now} is earlier than the last update
     */
    public void updateMetadata(MapMetadata mapMetadata, Instant now) {
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        requireNotBeforeLastUpdate(now);

        this.mapMetadata = mapMetadata;
        this.lastUpdatedAt = now;
    }

    /**
     * Adds a line without changing {@code lastUpdatedAt}. Use {@link #addLine(Line, Instant)} to record the
     * change as an update of the map. A line without a {@link LineId} is only indexed by its external id.
     *
     * @throws NullPointerException if {@code line} is {@code null}
     * @throws DuplicateLineException if the map already has a line with the same external id or {@link LineId}
     */
    public void addLine(Line line) {
        index(line);
    }

    /**
     * Adds a line and sets {@code now} as the last update of the map. A line without a {@link LineId} is
     * only indexed by its external id.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code now} is earlier than the last update
     * @throws DuplicateLineException if the map already has a line with the same external id or {@link LineId}
     */
    public void addLine(Line line, Instant now) {
        requireNotBeforeLastUpdate(now);

        index(line);
        this.lastUpdatedAt = now;
    }

    /**
     * Applies the changes to the line with the given {@link LineId}. Only the components of the changes
     * that are not {@code null} are applied, and the ids of the line never change.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code now} is earlier than the last update
     * @throws LineNotFoundException if the map has no line with that id
     */
    public void updateLine(LineId lineId, LineChanges lineChanges, Instant now) {
        requireNotBeforeLastUpdate(now);

        applyChanges(findLine(lineId), lineChanges, now);
    }

    /**
     * Applies the changes to the line with the given external id, which is stripped and case sensitive. Only
     * the components of the changes that are not {@code null} are applied, and the ids of the line never change.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code externalId} is empty or blank, or if {@code now} is earlier
     *                                  than the last update
     * @throws LineNotFoundException if the map has no line with that external id
     */
    public void updateLine(String externalId, LineChanges lineChanges, Instant now) {
        requireNotBeforeLastUpdate(now);

        applyChanges(findLine(externalId), lineChanges, now);
    }

    /**
     * Removes the line with the given {@link LineId} from the map. A line with the same ids can be added
     * again afterwards.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code now} is earlier than the last update
     * @throws LineNotFoundException if the map has no line with that id
     */
    public void removeLine(LineId lineId, Instant now) {
        requireNotBeforeLastUpdate(now);

        remove(findLine(lineId), now);
    }

    /**
     * Removes the line with the given external id, which is stripped and case sensitive, from the map.
     * A line with the same ids can be added again afterwards.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code externalId} is empty or blank, or if {@code now} is earlier
     *                                  than the last update
     * @throws LineNotFoundException if the map has no line with that external id
     */
    public void removeLine(String externalId, Instant now) {
        requireNotBeforeLastUpdate(now);

        remove(findLine(externalId), now);
    }

    private static TransportMap build(MapId id, MapMetadata mapMetadata, Instant lastUpdatedAt, Collection<Line> lines) {
        Objects.requireNonNull(id, "id should not be null");
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        Objects.requireNonNull(lastUpdatedAt, "last updated at should not be null");
        Objects.requireNonNull(lines, "lines should not be null");

        TransportMap map = new TransportMap(id, mapMetadata, lastUpdatedAt);
        lines.forEach(map::index);
        return map;
    }

    private static void requirePersisted(Line line) {
        Objects.requireNonNull(line, "line should not be null");
        if (line.getId() == null) {
            throw new IllegalArgumentException("a reconstituted line should have an id");
        }
    }

    private void index(Line line) {
        Objects.requireNonNull(line, "line should not be null");

        if (linesByExternalId.containsKey(line.getExternalId())) {
            throw new DuplicateLineException("a line with external id " + line.getExternalId() + " already exists");
        }
        if (line.getId() != null && lines.containsKey(line.getId())) {
            throw new DuplicateLineException("a line with id " + line.getId().value() + " already exists");
        }

        linesByExternalId.put(line.getExternalId(), line);
        if (line.getId() != null) {
            lines.put(line.getId(), line);
        }
    }

    private void unindex(Line line) {
        linesByExternalId.remove(line.getExternalId());
        if (line.getId() != null) {
            lines.remove(line.getId());
        }
    }

    private void applyChanges(Line line, LineChanges lineChanges, Instant now) {
        line.update(lineChanges);
        this.lastUpdatedAt = now;
    }

    private void remove(Line line, Instant now) {
        unindex(line);
        this.lastUpdatedAt = now;
    }

    private void requireNotBeforeLastUpdate(Instant now) {
        Objects.requireNonNull(now, "now should not be null");
        if (now.isBefore(lastUpdatedAt)) {
            throw new IllegalArgumentException("now cannot be before the last update");
        }
    }
}

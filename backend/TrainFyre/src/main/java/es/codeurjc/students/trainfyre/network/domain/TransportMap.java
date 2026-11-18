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

    public static TransportMap createTransportMap(MapMetadata mapMetadata, Instant now) {
        return createTransportMap(mapMetadata, List.of(), now);
    }

    public static TransportMap createTransportMap(MapMetadata mapMetadata, Collection<Line> lines, Instant now) {
        return build(MapId.generate(), mapMetadata, now, lines);
    }

    public static TransportMap reconstitute(MapId id, MapMetadata mapMetadata, Instant lastUpdatedAt, Collection<Line> lines) {
        Objects.requireNonNull(lines, "lines should not be null");
        lines.forEach(TransportMap::requirePersisted);

        return build(id, mapMetadata, lastUpdatedAt, lines);
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

    public Collection<Line> lines() {
        return Collections.unmodifiableCollection(lines.values());
    }

    public Collection<Line> linesByExternalId() {
        return Collections.unmodifiableCollection(linesByExternalId.values());
    }

    public void updateMetadata(MapMetadata mapMetadata, Instant now) {
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        requireNotBeforeLastUpdate(now);

        this.mapMetadata = mapMetadata;
        this.lastUpdatedAt = now;
    }

    public void addLine(Line line) {
        index(line);
    }

    public void addLine(Line line, Instant now) {
        requireNotBeforeLastUpdate(now);

        index(line);
        this.lastUpdatedAt = now;
    }

    public Line findLine(LineId lineId) {
        Objects.requireNonNull(lineId, "line id should not be null");

        Line line = lines.get(lineId);
        if (line == null) {
            throw new LineNotFoundException("no line with id " + lineId.value() + " exists");
        }
        return line;
    }

    public Line findLine(String externalId) {
        String normalizedExternalId = Line.normalizeExternalId(externalId);

        Line line = linesByExternalId.get(normalizedExternalId);
        if (line == null) {
            throw new LineNotFoundException("no line with external id " + normalizedExternalId + " exists");
        }
        return line;
    }

    public void updateLine(LineId lineId, LineChanges lineChanges, Instant now) {
        throw new UnsupportedOperationException();
    }

    public void updateLine(String externalId, LineChanges lineChanges, Instant now) {
        throw new UnsupportedOperationException();
    }

    private void requireNotBeforeLastUpdate(Instant now) {
        Objects.requireNonNull(now, "now should not be null");
        if (now.isBefore(lastUpdatedAt)) {
            throw new IllegalArgumentException("now cannot be before the last update");
        }
    }
}

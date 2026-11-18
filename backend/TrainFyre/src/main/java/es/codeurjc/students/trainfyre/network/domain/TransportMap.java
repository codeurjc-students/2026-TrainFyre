package es.codeurjc.students.trainfyre.network.domain;

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
public class TransportMap {

    @Identity
    @Getter
    private final MapId id;
    @Getter
    private MapMetadata mapMetadata;
    @Getter
    private Instant lastUpdatedAt;
    private final Map<LineId, Line> lines;
    private final Map<String, Line> linesByExternalId;

    private TransportMap(MapId id, MapMetadata mapMetadata, Instant lastUpdatedAt, Map<LineId, Line> lines, Map<String, Line> linesByExternalId) {
        this.id = id;
        this.mapMetadata = mapMetadata;
        this.lastUpdatedAt = lastUpdatedAt;
        this.lines = lines;
        this.linesByExternalId = linesByExternalId;
    }

    public static TransportMap createTransportMap(MapMetadata mapMetadata, Instant lastUpdatedAt) {
        return createTransportMap(mapMetadata, List.of(), lastUpdatedAt);
    }

    public static TransportMap createTransportMap(MapMetadata mapMetadata, Collection<Line> lines, Instant lastUpdatedAt) {
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        Objects.requireNonNull(lines, "lines should not be null");
        Objects.requireNonNull(lastUpdatedAt, "last updated at should not be null");

        TransportMap map = new TransportMap(MapId.generate(), mapMetadata, lastUpdatedAt, new HashMap<>(), new HashMap<>());
        lines.forEach(map::index);
        return map;
    }

    public static TransportMap reconstitute(MapId id, MapMetadata mapMetadata, Instant lastUpdatedAt, Collection<Line> lines) {
        throw new UnsupportedOperationException();
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
}

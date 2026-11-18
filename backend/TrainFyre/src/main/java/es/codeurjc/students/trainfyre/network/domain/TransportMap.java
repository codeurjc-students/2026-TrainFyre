package es.codeurjc.students.trainfyre.network.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    public static TransportMap createTransportMap(MapMetadata mapMetadata) {
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");

        return new TransportMap(MapId.generate(), mapMetadata, null, new HashMap<>(), new HashMap<>());
    }

    public Collection<Line> lines() {
        return Collections.unmodifiableCollection(lines.values());
    }
}

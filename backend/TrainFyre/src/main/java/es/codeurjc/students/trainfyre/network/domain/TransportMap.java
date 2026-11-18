package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.Map;

@AggregateRoot
public class TransportMap {

    @Identity
    private final MapId id;
    private MapMetadata mapMetadata;
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
        return null;
    }
}

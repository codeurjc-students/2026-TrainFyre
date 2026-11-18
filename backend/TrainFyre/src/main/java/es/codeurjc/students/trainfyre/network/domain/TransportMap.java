package es.codeurjc.students.trainfyre.network.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AggregateRoot
@Getter
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

        Objects.requireNonNull(mapMetadata, "map metadata should not be null");

        return new TransportMap(MapId.generate(), mapMetadata, null, new HashMap<LineId, Line>(), new HashMap<String, Line>());
    }
}

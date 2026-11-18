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

    public static TransportMap createTransportMap(MapMetadata mapMetadata, Collection<Line> lines){

        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        Objects.requireNonNull(lines, "lines shoul not be null");

        Map<LineId, Line> linesById = new HashMap<>();
        Map<String, Line> linesByExternalId = new HashMap<>();

        for(Line line : lines){

            if(line.getId() != null) {
                if(linesById.containsKey(line.getId())) throw new DuplicateLineException("A line with id: " + line.getId() + " already exists");
                linesById.put(line.getId(), line);
            }

            if(linesByExternalId.containsKey(line.getExternalId())) throw new DuplicateLineException("A line with external id: " + line.getId() + " already exists");
            linesByExternalId.put(line.getExternalId(), line);

        }

        return new TransportMap(MapId.generate(), mapMetadata, null, linesById, linesByExternalId);
    }

    public Collection<Line> lines() {
        return Collections.unmodifiableCollection(lines.values());
    }

    public Collection<Line> linesByExternalId() {
        return Collections.unmodifiableCollection(linesByExternalId.values());
    }
}

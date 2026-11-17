package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

@AggregateRoot
public class TransportMap {

    @Identity
    private final MapId id;
    private String name;
    private ZoneId timezone;
    private DataSource source;
    private String country;
    private URI website;
    private Instant lastUpdatedAt;
    private final Map<LineId, Line> lines;
    private final Map<String, Line> linesByExternalId;


    private TransportMap(MapId id, String name, ZoneId timezone, DataSource source, String country, URI website, Instant lastUpdatedAt, Map<LineId, Line> lines, Map<String, Line> linesByExternalId) {
        this.id = id;
        this.name = name;
        this.timezone = timezone;
        this.source = source;
        this.country = country;
        this.website = website;
        this.lastUpdatedAt = lastUpdatedAt;
        this.lines = lines;
        this.linesByExternalId = linesByExternalId;
    }
}

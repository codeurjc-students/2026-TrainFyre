package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

@AggregateRoot
public class TransportMap {

    //private final MapId id;
    private String name;
    private ZoneId timezone;
    private DataSource source;
    private String country;
    private URI website;
    private Instant lastUpdatedAt;
    //private final Map<LineId, Line> lines;
    //private final Map<String, Line> linesByExternalId;

}

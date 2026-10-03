package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.statistics.domain.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "incidence")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class IncidenceEntity {

    @Id
    private UUID id;

    @Column(name = "map_id", nullable = false)
    private Long mapId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "line_ids", nullable = false)
    private List<Long> lineIds;

    private ZonedDateTime timestamp;
    private Duration duration;
    private String name;
    private String summary;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private Cause cause;


    public IncidenceEntity(Incidence incidence){
        AffectedNetwork affectedNetwork = incidence.getAffectedNetwork();
        Occurrence occurrence = incidence.getOccurrence();
        Description description = incidence.getDescription();
        Classification classification = incidence.getClassification();
        this(
                incidence.getId(),
                affectedNetwork.mapId(),
                List.copyOf(affectedNetwork.lineIds()),
                occurrence.timestamp(),
                occurrence.duration(),
                description.name(),
                description.summary(),
                classification.severity(),
                classification.cause()
        );
    }

    public Incidence toDomain() {
        return Incidence.reconstitute(
                id,
                new AffectedNetwork(mapId, List.copyOf(lineIds)),
                new Occurrence(timestamp, duration),
                new Description(name, summary),
                new Classification(severity, cause)
        );
    }

}
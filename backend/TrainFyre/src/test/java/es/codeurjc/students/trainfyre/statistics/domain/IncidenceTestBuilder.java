package es.codeurjc.students.trainfyre.statistics.domain;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class IncidenceTestBuilder {

    private AffectedNetwork affectedNetwork = new AffectedNetwork(1L, List.of(1L, 2L, 3L));

    private Occurrence occurrence =
            new Occurrence(
                    ZonedDateTime.parse("2026-01-01T10:00:00Z"),
                    Duration.ofMinutes(30)
            );

    private Description description =
            new Description(
                    "Train failure",
                    "The train's engine has broken down and won't start."
            );

    private Classification classification =
            new Classification(
                    Severity.MODERATE,
                    Cause.MEDICAL_EMERGENCY
            );

    public static IncidenceTestBuilder anIncidence() {
        return new IncidenceTestBuilder();
    }

    public IncidenceTestBuilder withAffectedNetwork(
            AffectedNetwork affectedNetwork
    ) {
        this.affectedNetwork = affectedNetwork;
        return this;
    }

    public IncidenceTestBuilder withOccurrence(Occurrence occurrence) {
        this.occurrence = occurrence;
        return this;
    }

    public IncidenceTestBuilder withDescription(Description description) {
        this.description = description;
        return this;
    }

    public IncidenceTestBuilder withClassification(
            Classification classification
    ) {
        this.classification = classification;
        return this;
    }

    public Incidence build() {
        return Incidence.createIncidence(
                affectedNetwork,
                occurrence,
                description,
                classification
        );
    }
}

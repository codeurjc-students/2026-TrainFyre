package es.codeurjc.students.trainfyre.statistics.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Objects;
import java.util.UUID;

@AggregateRoot
@Getter
public class Incidence {

    @Identity
    private final UUID id;
    private AffectedNetwork affectedNetwork;
    private Occurrence occurrence;
    private Description description;
    private Classification classification;

    private Incidence(UUID id, AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification) {
        this.id = id;
        this.affectedNetwork = affectedNetwork;
        this.occurrence = occurrence;
        this.description = description;
        this.classification = classification;
    }

    public static Incidence createIncidence(AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification){

        if (affectedNetwork == null) throw new IllegalArgumentException("affected network should not be null");
        if (occurrence == null) throw new IllegalArgumentException("occurrence should not be null");
        if (description == null) throw new IllegalArgumentException("description should not be null");
        if (classification == null) throw new IllegalArgumentException("classification should not be null");
        ensureInformationalIncidenceIsInstantaneous(classification, occurrence);

        UUID id = UUID.randomUUID();

        return new Incidence(id, affectedNetwork, occurrence, description, classification);

    }

    public static Incidence reconstitute(
            UUID id,
            AffectedNetwork affectedNetwork,
            Occurrence occurrence,
            Description description,
            Classification classification
    ) {
        Objects.requireNonNull(id, "id should not be null");
        Objects.requireNonNull(affectedNetwork, "affected network should not be null");
        Objects.requireNonNull(occurrence, "occurrence should not be null");
        Objects.requireNonNull(description, "description should not be null");
        Objects.requireNonNull(classification, "classification should not be null");
        ensureInformationalIncidenceIsInstantaneous(classification, occurrence);

        return new Incidence(id, affectedNetwork, occurrence, description, classification);
    }


    public void changeAffectedNetwork(AffectedNetwork newAffectedNetwork) {
        if (newAffectedNetwork == null) throw new IllegalArgumentException("affected network should not be null");
        this.affectedNetwork = newAffectedNetwork;
    }

    public void changeOccurrence(Occurrence newOccurrence) {
        if (newOccurrence == null) throw new IllegalArgumentException("occurrence should not be null");
        ensureInformationalIncidenceIsInstantaneous(classification, newOccurrence);
        this.occurrence = newOccurrence;
    }

    public void changeDescription(Description newDescription){
        if (newDescription == null) throw new IllegalArgumentException("description should not be null");
        this.description = newDescription;
    }

    public void changeClassification(Classification newClassification) {
        if (newClassification == null) throw new IllegalArgumentException("classification should not be null");
        ensureInformationalIncidenceIsInstantaneous(newClassification, this.occurrence);
        this.classification = newClassification;
    }

    public IncidenceDetails getIncidenceDetails(){
        return new IncidenceDetails(affectedNetwork, occurrence, description, classification);
    }

    private static void ensureInformationalIncidenceIsInstantaneous(Classification classification, Occurrence occurrence){
        if(classification.severity().equals(Severity.INFORMATIONAL) && !occurrence.isInstantaneous()) throw new IllegalArgumentException("an informational incidence must have Duration.ZERO");
    }
}

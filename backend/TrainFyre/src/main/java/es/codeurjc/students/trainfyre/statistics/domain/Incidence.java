package es.codeurjc.students.trainfyre.statistics.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

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
        if(classification.severity().equals(Severity.INFORMATIONAL) && !occurrence.isInstantaneous()) throw new IllegalArgumentException("an informational incidence must have Duration.ZERO");

        UUID id = UUID.randomUUID();

        return new Incidence(id, affectedNetwork, occurrence, description, classification);

    }

    public void changeAffectedNetwork(AffectedNetwork newAffectedNetwork) {
        if (newAffectedNetwork == null) throw new IllegalArgumentException("affected network should not be null");
        this.affectedNetwork = newAffectedNetwork;
    }

    public void changeOccurrence(Occurrence newOccurrence) {
        if (newOccurrence == null) throw new IllegalArgumentException("occurrence should not be null");
        if(this.classification.severity().equals(Severity.INFORMATIONAL) && !newOccurrence.isInstantaneous()) throw new IllegalArgumentException("an informational incidence cannot change duration to other different than zero, must have Duration.ZERO");
        this.occurrence = newOccurrence;
    }

    public void changeDescription(Description newDescription){
        if (newDescription == null) throw new IllegalArgumentException("description should not be null");
        this.description = newDescription;
    }

    public void changeClassification(Classification newClassification) {
        if (newClassification == null) throw new IllegalArgumentException("classification should not be null");
        this.classification = newClassification;
    }
}

package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.UUID;

@Entity
public class Incidence {

    @Identity
    private UUID id;
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

        UUID id = UUID.randomUUID();

        return new Incidence(id, affectedNetwork, occurrence, description, classification);

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AffectedNetwork getAffectedNetwork() {
        return affectedNetwork;
    }

    public void setAffectedNetwork(AffectedNetwork affectedNetwork) {
        this.affectedNetwork = affectedNetwork;
    }

    public Occurrence getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Occurrence occurrence) {
        this.occurrence = occurrence;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }
}

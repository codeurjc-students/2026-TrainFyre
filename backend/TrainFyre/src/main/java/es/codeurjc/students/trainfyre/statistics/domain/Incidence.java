package es.codeurjc.students.trainfyre.statistics.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.UUID;

@Entity
@Getter
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

        if (affectedNetwork == null) throw new IllegalArgumentException("affected network should not be null");
        if (occurrence == null) throw new IllegalArgumentException("occurrence should not be null");

        UUID id = UUID.randomUUID();

        return new Incidence(id, affectedNetwork, occurrence, description, classification);

    }
}

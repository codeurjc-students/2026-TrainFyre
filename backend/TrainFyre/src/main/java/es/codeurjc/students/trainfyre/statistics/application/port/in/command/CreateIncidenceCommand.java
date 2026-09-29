package es.codeurjc.students.trainfyre.statistics.application.port.in.command;

import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record CreateIncidenceCommand(AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification){

    public CreateIncidenceCommand {
        Objects.requireNonNull(affectedNetwork, "cannot create a CreateIncidenceCommand with null AffectedNetwork");
        Objects.requireNonNull(occurrence, "cannot create a CreateIncidenceCommand with null Occurrence");
        Objects.requireNonNull(description, "cannot create a CreateIncidenceCommand with null Description");
        Objects.requireNonNull(classification, "cannot create a CreateIncidenceCommand with null Classification");
    }

}

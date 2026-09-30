package es.codeurjc.students.trainfyre.statistics.application.port.in.command;

import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;
import java.util.UUID;

@Command
public record UpdateIncidenceCommand(UUID uuid, AffectedNetwork changeAffectedNetwork, Occurrence changeOccurrence, Description changeDescription, Classification changeClassification) {

    public UpdateIncidenceCommand {
        Objects.requireNonNull(uuid, "id should not be null");
        if(changeAffectedNetwork == null &&
                changeOccurrence == null &&
                changeDescription == null &&
                changeClassification == null)
        {throw new NullPointerException("At least one argument must not be null");}
    }
}

package es.codeurjc.students.trainfyre.statistics.application.port.in.command;

import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;
import org.jmolecules.architecture.cqrs.Command;

import java.util.UUID;

@Command
public record UpdateIncidenceCommand(UUID uuid, AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification) {
}

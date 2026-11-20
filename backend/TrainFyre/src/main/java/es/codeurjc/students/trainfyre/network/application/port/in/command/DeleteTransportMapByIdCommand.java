package es.codeurjc.students.trainfyre.network.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.UUID;

@Command
public record DeleteTransportMapByIdCommand(UUID id) {
}

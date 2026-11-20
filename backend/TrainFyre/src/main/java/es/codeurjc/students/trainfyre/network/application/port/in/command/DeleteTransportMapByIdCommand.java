package es.codeurjc.students.trainfyre.network.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;
import java.util.UUID;

@Command
public record DeleteTransportMapByIdCommand(UUID id) {

    public DeleteTransportMapByIdCommand {
        Objects.requireNonNull(id, "id sould not be null");
    }
}

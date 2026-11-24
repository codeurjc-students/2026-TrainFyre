package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record DeleteTransportMapCommand(MapRef map) {

    public DeleteTransportMapCommand {
        Objects.requireNonNull(map, "map should not be null");
    }
}

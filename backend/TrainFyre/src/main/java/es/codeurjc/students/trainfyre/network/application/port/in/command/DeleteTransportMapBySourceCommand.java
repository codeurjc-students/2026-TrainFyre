package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record DeleteTransportMapBySourceCommand(DataSource source) {

    public DeleteTransportMapBySourceCommand {
        Objects.requireNonNull(source, "source should not be null");
    }
}

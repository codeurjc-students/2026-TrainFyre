package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;

public record DeleteTransportMapBySourceCommand(DataSource source) {
}

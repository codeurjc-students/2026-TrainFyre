package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;

import java.time.Instant;

public record UpdateTransportMapBySourceCommand(DataSource source, MapMetadata mapMetadata, Instant now) {
}

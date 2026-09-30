package es.codeurjc.students.trainfyre.statistics.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.UUID;

@Command
public record DeleteIncidenceCommand(UUID id) {}

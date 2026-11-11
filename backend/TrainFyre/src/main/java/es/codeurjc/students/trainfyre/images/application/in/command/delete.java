package es.codeurjc.students.trainfyre.images.application.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.UUID;

@Command
public record delete(UUID uuid) {
}

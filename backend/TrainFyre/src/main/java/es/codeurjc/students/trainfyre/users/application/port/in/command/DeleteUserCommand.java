package es.codeurjc.students.trainfyre.users.application.port.in.command;

import java.util.UUID;

public record DeleteUserCommand(UUID id) {
}

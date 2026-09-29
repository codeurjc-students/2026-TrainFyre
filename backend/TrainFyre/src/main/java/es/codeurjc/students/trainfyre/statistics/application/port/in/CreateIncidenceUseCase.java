package es.codeurjc.students.trainfyre.statistics.application.port.in;

import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.common.UseCase;

import java.util.UUID;

public interface CreateIncidenceUseCase extends UseCase<CreateIncidenceCommand, UUID>{}

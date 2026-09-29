package es.codeurjc.students.trainfyre.statistics.application.port.out;

import es.codeurjc.students.trainfyre.common.EntityPort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface IncidencePort extends EntityPort<Incidence> { }

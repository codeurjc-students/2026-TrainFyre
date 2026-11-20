package es.codeurjc.students.trainfyre.network.application.port.out;

import es.codeurjc.students.trainfyre.common.DeletePort;
import es.codeurjc.students.trainfyre.common.EntityPort;
import es.codeurjc.students.trainfyre.common.SlicedPort;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

import java.util.UUID;

@SecondaryPort
public interface TransportMapPort extends EntityPort<TransportMap, UUID>, DeletePort<UUID>, SlicedPort<TransportMap> {
}

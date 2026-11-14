package es.codeurjc.students.trainfyre.images.application.port.out;

import es.codeurjc.students.trainfyre.images.domain.Image;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

import java.util.Optional;
import java.util.UUID;

@SecondaryPort
public interface ImagePort {

    void save(Image image);

    Optional<Image> find(UUID uuid);

    void delete(UUID uuid);

}

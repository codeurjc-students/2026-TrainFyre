package es.codeurjc.students.trainfyre.network.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.SliceResponse;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Placeholder so the Spring context can start. Every operation fails loudly until the real persistence exists.
 */
@SecondaryAdapter
@Repository
public class TransportMapJPARepository implements TransportMapPort {

    private static final String NOT_IMPLEMENTED = "not implemented yet";

    @Override
    public Optional<TransportMap> findBySource(DataSource source) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public Void delete(UUID id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void save(TransportMap entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public TransportMap findById(UUID id, Class<TransportMap> transportMapClass) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Finds the slice at the page and size of the given {@link Pageable}. The first page is 0.
     * The result says whether another slice follows, but not how many elements exist in total.
     */
    @Override
    public SliceResponse<TransportMap> findSlice(Pageable pageable) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}

package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.network.domain.DataSource;

import java.util.Objects;
import java.util.UUID;

/**
 * Way to identify an existing transport map in a use case: by its id or by its {@link DataSource}.
 */
public sealed interface MapRef {

    record ById(UUID id) implements MapRef {

        public ById {
            Objects.requireNonNull(id, "id should not be null");
        }
    }

    record BySource(DataSource source) implements MapRef {

        public BySource {
            Objects.requireNonNull(source, "source should not be null");
        }
    }
}

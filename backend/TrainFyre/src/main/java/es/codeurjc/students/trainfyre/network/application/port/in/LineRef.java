package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.network.domain.LineId;

import java.util.Objects;

/**
 * Way to identify an existing line of a transport map in a use case: by its {@link LineId} or by its
 * external id.
 */
public sealed interface LineRef {

    record ById(LineId id) implements LineRef {

        public ById {
            Objects.requireNonNull(id, "id should not be null");
        }
    }

    record ByExternalId(String externalId) implements LineRef {

        public ByExternalId {
            externalId = normalizeExternalId(externalId);
        }

        private String normalizeExternalId(String externalId) {
            Objects.requireNonNull(externalId, "external id should not be null");

            String normalized = externalId.strip();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("external id cannot be blank");
            }
            return normalized;
        }
    }
}

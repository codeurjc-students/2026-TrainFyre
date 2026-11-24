package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.network.domain.LineId;

import java.util.Objects;

public sealed interface LineRef {


    record ById(LineId id) implements LineRef {

        public ById {
            Objects.requireNonNull(id, "id should not be null");
        }
    }


    record ByExternalId(String externalId) implements LineRef{

        public ByExternalId {
            Objects.requireNonNull(externalId, "external id should not be null");
        }
    }


}

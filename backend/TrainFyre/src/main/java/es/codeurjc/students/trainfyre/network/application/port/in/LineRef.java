package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.network.domain.LineId;

public sealed interface LineRef {


    record ById(LineId id) implements LineRef {}


    record ByExternalId(String externalId) implements LineRef{}


}

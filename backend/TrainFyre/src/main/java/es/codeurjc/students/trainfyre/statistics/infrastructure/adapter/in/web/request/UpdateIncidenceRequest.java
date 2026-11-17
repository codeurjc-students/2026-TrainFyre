package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web.request;

import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;

public record UpdateIncidenceRequest(AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification) {
}

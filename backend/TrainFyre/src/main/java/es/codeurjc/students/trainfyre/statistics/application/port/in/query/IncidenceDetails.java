package es.codeurjc.students.trainfyre.statistics.application.port.in.query;

import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;

public record IncidenceDetails(AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification) {}

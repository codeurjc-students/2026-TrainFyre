package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.domain.DataSource;

public record GetTransportMapBySourceQuery(DataSource dataSource) {
}

package es.codeurjc.students.trainfyre.statistics.infrastructure.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.GetIncidenceByIdUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import es.codeurjc.students.trainfyre.statistics.domain.IncidenceDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class GetIncidenceByIdServiceIntegrationTest {

    @Autowired
    private GetIncidenceByIdUseCase getIncidenceByIdUseCase;

    @MockitoBean
    private IncidencePort incidencePort;

    @Test
    void shouldReturnIncidenceDetailsThroughSpringContext() {
        Incidence incidence = anIncidence().build();
        var query = new GetIncidenceByIdQuery(incidence.getId());

        when(incidencePort.findById(incidence.getId(), Incidence.class))
                .thenReturn(incidence);

        IncidenceDetails details = getIncidenceByIdUseCase.execute(query);

        verify(incidencePort).findById(incidence.getId(), Incidence.class);
        assertThat(details.affectedNetwork()).isEqualTo(incidence.getAffectedNetwork());
        assertThat(details.occurrence()).isEqualTo(incidence.getOccurrence());
        assertThat(details.description()).isEqualTo(incidence.getDescription());
        assertThat(details.classification()).isEqualTo(incidence.getClassification());
    }
}

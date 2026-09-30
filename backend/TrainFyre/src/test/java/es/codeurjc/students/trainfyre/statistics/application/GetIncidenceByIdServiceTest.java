package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.application.port.in.GetIncidenceByIdUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.IncidenceDetails;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.application.service.GetIncidenceByIdService;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetIncidenceByIdServiceTest {

    @Mock
    private IncidencePort incidencePort;
    private GetIncidenceByIdUseCase getIncidenceByIdUseCase;


    @BeforeEach
    void setUp(){
        getIncidenceByIdUseCase = new GetIncidenceByIdService(incidencePort);
    }

    @Test
    void shouldReturnIncidenceDetails(){

        Incidence incidence = anIncidence().build();
        GetIncidenceByIdQuery getIncidenceByIdQuery = new GetIncidenceByIdQuery(incidence.getId());
        when(incidencePort.findById(incidence.getId(), Incidence.class)).thenReturn(incidence);

        IncidenceDetails incidenceDetails = getIncidenceByIdUseCase.execute(getIncidenceByIdQuery);

        assertThat(incidenceDetails.affectedNetwork()).isEqualTo(incidence.getAffectedNetwork());
        assertThat(incidenceDetails.occurrence()).isEqualTo(incidence.getOccurrence());
        assertThat(incidenceDetails.description()).isEqualTo(incidence.getDescription());
        assertThat(incidenceDetails.classification()).isEqualTo(incidence.getClassification());

    }
}

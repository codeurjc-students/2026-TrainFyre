package es.codeurjc.students.trainfyre.statistics.infrastructure;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.GetAllIncidencesPaginatedUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class GetAllIncidencesPaginatedServiceIntegrationTest {

    @Autowired
    private GetAllIncidencesPaginatedUseCase useCase;

    @MockitoBean
    private IncidencePort incidencePort;

    @Test
    void shouldReturnPaginatedIncidencesFromPortThroughSpringContext() {
        Incidence incidence1 = anIncidence()
                .withDescription(new Description("incidence1", "desc1")).build();
        Incidence incidence2 = anIncidence()
                .withDescription(new Description("incidence2", "desc2")).build();
        Incidence incidence3 = anIncidence()
                .withDescription(new Description("incidence3", "desc3")).build();

        Pageable pageable = new Pageable(3, 3);
        var query = new GetAllIncidencesPaginatedQuery(pageable);
        PagedResponse<Incidence> expected = new PagedResponse<>(
                List.of(incidence1, incidence2, incidence3), 3, 3, 3
        );

        when(incidencePort.findAll(pageable)).thenReturn(expected);

        PagedResponse<Incidence> actual = useCase.execute(query);

        verify(incidencePort).findAll(pageable);
        assertThat(actual).isSameAs(expected);
    }
}

package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.GetAllIncidencesPaginatedUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.application.service.GetAllIncidencesPaginatedService;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllIncidencesPaginatedServiceTest {

    @Mock
    private IncidencePort incidencePort;

    private GetAllIncidencesPaginatedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllIncidencesPaginatedService(incidencePort);
    }

    @Test
    void shouldReturnPaginatedIncidencesFromPort() {
        // Given
        Incidence incidence1 = anIncidence().withDescription(new Description("incidence1", "desc1")).build();
        Incidence incidence2 = anIncidence().withDescription(new Description("incidence2", "desc2")).build();
        Incidence incidence3 = anIncidence().withDescription(new Description("incidence3", "desc3")).build();

        Pageable pageable = new Pageable(3, 3);
        GetAllIncidencesPaginatedQuery query = new GetAllIncidencesPaginatedQuery(pageable);
        PagedResponse<Incidence> expected = new PagedResponse<>(List.of(incidence1, incidence2, incidence3), 3, 3, 3);

        when(incidencePort.findAll(pageable)).thenReturn(expected);

        // When
        PagedResponse<Incidence> actual = useCase.execute(query);

        // Then
        verify(incidencePort).findAll(pageable);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}

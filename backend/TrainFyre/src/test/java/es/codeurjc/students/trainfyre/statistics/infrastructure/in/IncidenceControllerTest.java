package es.codeurjc.students.trainfyre.statistics.infrastructure.in;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.*;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.domain.*;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web.IncidenceController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IncidenceControllerTest {

    @Mock
    private CreateIncidenceUseCase createIncidenceUseCase;
    @Mock
    private GetAllIncidencesPaginatedUseCase getAllIncidencesPaginatedUseCase;
    @Mock
    private GetIncidenceByIdUseCase getIncidenceByIdUseCase;
    @Mock
    private UpdateIncidenceUseCase updateIncidenceUseCase;
    @Mock
    private DeleteIncidenceUseCase deleteIncidenceUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new IncidenceController(createIncidenceUseCase, getAllIncidencesPaginatedUseCase, getIncidenceByIdUseCase, updateIncidenceUseCase, deleteIncidenceUseCase))
                .build();
    }

    @Test
    void shouldCreateAnIncidenceAndReturnId() throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        CreateIncidenceCommand createIncidenceCommand = new CreateIncidenceCommand(
                new AffectedNetwork(1L, List.of(2L, 3L)),
                new Occurrence(
                        ZonedDateTime.parse("2026-09-30T10:00:00Z"),
                        Duration.ofMinutes(5)
                ),
                new Description("Avería", "Interrupción del servicio"),
                new Classification(Severity.MODERATE, Cause.WEATHER)
        );

        when(createIncidenceUseCase.execute(createIncidenceCommand)).thenReturn(id);

        String json = """
                {
                  "affectedNetwork": {
                    "mapId": 1,
                    "lineIds": [2, 3]
                  },
                  "occurrence": {
                    "timestamp": "2026-09-30T10:00:00Z",
                    "duration": "PT5M"
                  },
                  "description": {
                    "name": "Avería",
                    "summary": "Interrupción del servicio"
                  },
                  "classification": {
                    "severity": "%s",
                    "cause": "%s"
                  }
                }
                """.formatted(Severity.MODERATE.name(), Cause.WEATHER.name());

        mockMvc.perform(post("/incidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + id + "\""));

        verify(createIncidenceUseCase).execute(createIncidenceCommand);
    }

    @Test
    void shouldGetIncidencesPaginated() throws Exception {

        GetAllIncidencesPaginatedQuery getAllIncidencesPaginatedQuery = new GetAllIncidencesPaginatedQuery(new Pageable(0, 10));

        when(getAllIncidencesPaginatedUseCase.execute(getAllIncidencesPaginatedQuery))
                .thenReturn(
                        new PagedResponse<>(
                                List.of(anIncidence().build()),
                                0,
                                10,
                                1
                        )
                );

        mockMvc.perform(get("/incidence")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());


        verify(getAllIncidencesPaginatedUseCase).execute(getAllIncidencesPaginatedQuery);
    }

    @Test
    void shouldGetIncidenceById() throws Exception {

        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        GetIncidenceByIdQuery getIncidenceByIdQuery = new GetIncidenceByIdQuery(id);
        Incidence incidence = anIncidence().build();


        when(getIncidenceByIdUseCase.execute(getIncidenceByIdQuery)).thenReturn(incidence.getIncidenceDetails());

        mockMvc.perform(get("/incidence/{id}", id))
                .andExpect(status().isOk());

        verify(getIncidenceByIdUseCase).execute(getIncidenceByIdQuery);

    }

    @Test
    void shouldUpdateIncidence() throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UpdateIncidenceCommand command = new UpdateIncidenceCommand(
                id,
                null,
                null,
                new Description("Avería actualizada", "Servicio restablecido"),
                null
        );

        String json = """
            {
              "uuid": "123e4567-e89b-12d3-a456-426614174000",
              "changeDescription": {
                "name": "Avería actualizada",
                "summary": "Servicio restablecido"
              }
            }
            """;

        mockMvc.perform(put("/incidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(updateIncidenceUseCase).execute(command);
    }

    @Test
    void shouldDeleteIncidence() throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        DeleteIncidenceCommand command = new DeleteIncidenceCommand(id);

        String json = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        mockMvc.perform(delete("/incidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteIncidenceUseCase).execute(command);
    }



}

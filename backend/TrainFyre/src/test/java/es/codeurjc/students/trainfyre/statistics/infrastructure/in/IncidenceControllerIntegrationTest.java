package es.codeurjc.students.trainfyre.statistics.infrastructure.in;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IncidenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private IncidencePort incidencePort;

    @Test
    void shouldCreateReadUpdateListAndDeleteUsingRealServices() throws Exception {
        // El puerto simulado conserva las incidencias solo durante este test.
        Map<UUID, Incidence> incidences = new LinkedHashMap<>();

        doAnswer(invocation -> {
            Incidence incidence = invocation.getArgument(0);
            incidences.put(incidence.getId(), incidence);
            return null;
        }).when(incidencePort).save(any(Incidence.class));

        when(incidencePort.findById(any(UUID.class), eq(Incidence.class)))
                .thenAnswer(invocation -> incidences.get(invocation.getArgument(0)));

        when(incidencePort.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PagedResponse<>(
                        List.copyOf(incidences.values()), 0, 10, incidences.size()
                ));

        doAnswer(invocation -> {
            incidences.remove(invocation.getArgument(0));
            return null;
        }).when(incidencePort).delete(any(UUID.class));

        // POST: el controlador llama al servicio real, que crea y guarda la incidencia.
        CreateIncidenceCommand createCommand = new CreateIncidenceCommand(
                new AffectedNetwork(1L, List.of(2L, 3L)),
                new Occurrence(
                        ZonedDateTime.parse("2026-09-30T10:00:00Z"),
                        Duration.ofMinutes(5)
                ),
                new Description("Avería", "Interrupción del servicio"),
                new Classification(Severity.MODERATE, Cause.WEATHER)
        );

        String createResponse = mockMvc.perform(post("/incidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommand)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID id = objectMapper.readValue(createResponse, UUID.class);
        assertThat(incidences).containsKey(id);
        assertThat(incidences.get(id).getDescription())
                .isEqualTo(createCommand.description());

        // GET /{id}: intervienen el controlador y GetIncidenceByIdService.
        mockMvc.perform(get("/incidence/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description.name").value("Avería"));

        // PUT: UpdateIncidenceService recupera la incidencia y modifica su descripción.
        UpdateIncidenceCommand updateCommand = new UpdateIncidenceCommand(
                id,
                null,
                null,
                new Description("Avería actualizada", "Servicio restablecido"),
                null
        );

        mockMvc.perform(put("/incidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCommand)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/incidence/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description.name").value("Avería actualizada"))
                .andExpect(jsonPath("$.description.summary").value("Servicio restablecido"));

        // GET paginado: se comprueba la respuesta devuelta por el servicio real.
        String listResponse = mockMvc.perform(get("/incidence")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var pageJson = objectMapper.readTree(listResponse);
        assertThat(pageJson.path("page").asInt()).isEqualTo(0);
        assertThat(pageJson.path("size").asInt()).isEqualTo(10);
        assertThat(pageJson.path("totalElements").asInt()).isEqualTo(1);
        assertThat(pageJson.path("content").size()).isEqualTo(1);

        var firstIncidence = pageJson.path("content").get(0);
        assertThat(firstIncidence.path("id").asText()).isEqualTo(id.toString());
        assertThat(firstIncidence.path("description").path("name").asText())
                .isEqualTo("Avería actualizada");
        assertThat(firstIncidence.path("description").path("summary").asText())
                .isEqualTo("Servicio restablecido");

        // DELETE: DeleteIncidenceService llama al puerto con el ID recibido.
        mockMvc.perform(delete("/incidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteIncidenceCommand(id)
                        )))
                .andExpect(status().isNoContent());

        assertThat(incidences).doesNotContainKey(id);

        verify(incidencePort, times(2)).save(any(Incidence.class));
        verify(incidencePort).findAll(new Pageable(0, 10));
        verify(incidencePort).delete(id);
    }
}

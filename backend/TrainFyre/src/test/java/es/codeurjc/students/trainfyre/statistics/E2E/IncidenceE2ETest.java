package es.codeurjc.students.trainfyre.statistics.E2E;

import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Cause;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;
import es.codeurjc.students.trainfyre.statistics.domain.Severity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
@Import(IncidenceE2ETest.PostgresConfig.class)
class IncidenceE2ETest {

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresConfig {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:16-alpine");
        }
    }

    @LocalServerPort
    int port;

    @Autowired
    JsonMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final List<UUID> createdIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (UUID id : createdIds) {
            jdbcTemplate.update("DELETE FROM incidence WHERE id = ?", id);
        }
        createdIds.clear();
    }

    @Test
    void shouldCreateIncidence() throws Exception {
        UUID id = createIncidence("Avería", "Interrupción del servicio");

        assertThat(id).isNotNull();

        // El GET por ID devuelve IncidenceDetails, sin campo "id".
        given()
                .port(port)
                .when()
                .get("/incidence/{id}", id)
                .then()
                .statusCode(200)
                .body("description.name", equalTo("Avería"))
                .body("description.summary", equalTo("Interrupción del servicio"));
    }

    @Test
    void shouldGetIncidenceById() throws Exception {
        UUID id = createIncidence("Avería", "Interrupción del servicio");

        given()
                .port(port)
                .when()
                .get("/incidence/{id}", id)
                .then()
                .statusCode(200)
                .body("description.name", equalTo("Avería"))
                .body("description.summary", equalTo("Interrupción del servicio"))
                .body("classification.severity", equalTo("MODERATE"))
                .body("classification.cause", equalTo("WEATHER"));
    }


    @Test
    void shouldUpdateIncidence() throws Exception {
        UUID id = createIncidence("Avería", "Interrupción del servicio");

        updateDescription(id, "Avería actualizada", "Servicio restablecido");

        given()
                .port(port)
                .when()
                .get("/incidence/{id}", id)
                .then()
                .statusCode(200)
                .body("description.name", equalTo("Avería actualizada"))
                .body("description.summary", equalTo("Servicio restablecido"));
    }

    @Test
    void shouldListIncidencesWithPagination() throws Exception {
        UUID firstId = createIncidence("Primera incidencia", "Primer resumen");
        UUID secondId = createIncidence("Segunda incidencia", "Segundo resumen");

        JsonNode firstPage = listPage(0, 1);
        JsonNode secondPage = listPage(1, 1);

        assertThat(firstPage.path("page").asInt()).isZero();
        assertThat(firstPage.path("size").asInt()).isEqualTo(1);
        assertThat(firstPage.path("totalElements").asInt()).isEqualTo(2);
        assertThat(firstPage.path("content").size()).isEqualTo(1);

        assertThat(secondPage.path("page").asInt()).isEqualTo(1);
        assertThat(secondPage.path("size").asInt()).isEqualTo(1);
        assertThat(secondPage.path("totalElements").asInt()).isEqualTo(2);
        assertThat(secondPage.path("content").size()).isEqualTo(1);

        // No suponemos un orden concreto si la API no lo garantiza.
        assertThat(List.of(
                firstPage.path("content").get(0).path("id").asText(),
                secondPage.path("content").get(0).path("id").asText()
        )).containsExactlyInAnyOrder(
                firstId.toString(),
                secondId.toString()
        );
    }

    @Test
    void shouldDeleteIncidence() throws Exception {
        UUID id = createIncidence("Incidencia a borrar", "Resumen");

        given()
                .port(port)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/incidence")
                .then()
                .statusCode(200)
                .body("content.id", hasItem(id.toString()));

        deleteIncidence(id);

        given()
                .port(port)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/incidence")
                .then()
                .statusCode(200)
                .body("content.id", not(hasItem(id.toString())));
    }

    @Test
    void shouldCompleteCreateReadUpdateListAndDeleteFlow() throws Exception {
        UUID id = createIncidence("Avería", "Interrupción del servicio");

        given()
                .port(port)
                .when()
                .get("/incidence/{id}", id)
                .then()
                .statusCode(200)
                .body("description.name", equalTo("Avería"));

        updateDescription(id, "Avería actualizada", "Servicio restablecido");

        given()
                .port(port)
                .when()
                .get("/incidence/{id}", id)
                .then()
                .statusCode(200)
                .body("description.name", equalTo("Avería actualizada"))
                .body("description.summary", equalTo("Servicio restablecido"));

        given()
                .port(port)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/incidence")
                .then()
                .statusCode(200)
                .body("content.id", hasItem(id.toString()));

        deleteIncidence(id);

        given()
                .port(port)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/incidence")
                .then()
                .statusCode(200)
                .body("content.id", not(hasItem(id.toString())));
    }

    private UUID createIncidence(String name, String summary) throws Exception {
        CreateIncidenceCommand command = new CreateIncidenceCommand(
                new AffectedNetwork(1L, List.of(2L, 3L)),
                new Occurrence(
                        ZonedDateTime.parse("2026-09-30T10:00:00Z"),
                        Duration.ofMinutes(5)
                ),
                new Description(name, summary),
                new Classification(Severity.MODERATE, Cause.WEATHER)
        );

        String response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(command))
                .when()
                .post("/incidence")
                .then()
                .statusCode(201)
                .extract().asString();

        UUID id = objectMapper.readValue(response, UUID.class);
        createdIds.add(id);
        return id;
    }

    private void updateDescription(UUID id, String name, String summary)
            throws Exception {
        UpdateIncidenceCommand command = new UpdateIncidenceCommand(
                id,
                null,
                null,
                new Description(name, summary),
                null
        );

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(command))
                .when()
                .put("/incidence")
                .then()
                .statusCode(204);
    }

    private void deleteIncidence(UUID id) throws Exception {
        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(
                        new DeleteIncidenceCommand(id)
                ))
                .when()
                .delete("/incidence")
                .then()
                .statusCode(204);
    }

    private JsonNode listPage(int page, int size) {
        String response = given()
                .port(port)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get("/incidence")
                .then()
                .statusCode(200)
                .extract().asString();

        return objectMapper.readTree(response);
    }
}

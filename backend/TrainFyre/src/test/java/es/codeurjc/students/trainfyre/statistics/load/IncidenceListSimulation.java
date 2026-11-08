package es.codeurjc.students.trainfyre.statistics.load;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.gatling.javaapi.core.Simulation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class IncidenceListSimulation extends Simulation {

    private final String baseUrl =
            System.getProperty("baseUrl", "https://localhost:443");

    private final int seedCount =
            Integer.getInteger("seedCount", 200);

    // Se rellena en before(), antes de que lleguen los usuarios virtuales.
    private volatile List<String> seededIds = List.of();

    public IncidenceListSimulation() {
        var httpProtocol = http
                .baseUrl(baseUrl)
                .acceptHeader("application/json");

        var browse = scenario("Consultar listado paginado")
                .repeat(3).on(
                        exec(http("GET /api/v1/incidence paginado")
                                .get("/api/v1/incidence")
                                .queryParam("page", session ->
                                        Integer.toString(
                                                ThreadLocalRandom.current()
                                                        .nextInt(
                                                                Math.max(
                                                                        1,
                                                                        (seedCount + 9) / 10
                                                                )
                                                        )
                                        ))
                                .queryParam("size", "10")
                                .check(status().is(200))
                                .check(jsonPath("$.content").exists()))
                                .pause(1)
                );

        var readDetails = scenario("Consultar incidencias existentes")
                .repeat(3).on(
                        exec(session -> {
                            var ids = seededIds;
                            String id = ids.get(
                                    ThreadLocalRandom.current().nextInt(ids.size())
                            );
                            return session.set("seededId", id);
                        })
                                .exec(http("GET /api/v1/incidence/{id} existente")
                                        .get("/api/v1/incidence/#{seededId}")
                                        .check(status().is(200))
                                        // IncidenceDetails no tiene campo id.
                                        .check(jsonPath("$.description.name").exists()))
                                .pause(1)
                );

        var lifecycle = scenario("Crear, consultar, actualizar y borrar")
                .exec(http("POST /api/v1/incidence")
                        .post("/api/v1/incidence")
                        .asJson()
                        .body(StringBody(session ->
                                createBody("Carga-" + UUID.randomUUID())
                        ))
                        .check(status().is(201))
                        .check(jsonPath("$").saveAs("createdId")))
                .exitHereIfFailed()
                .pause(1)
                .exec(http("GET /api/v1/incidence/#{createdId}")
                        .get("/api/v1/incidence/#{createdId}")
                        .check(status().is(200))
                        .check(jsonPath("$.description.name").exists()))
                .pause(1)
                .exec(http("PUT /api/v1/incidence")
                        .put("/api/v1/incidence")
                        .asJson()
                        .body(StringBody(session ->
                                updateBody(session.getString("createdId"))
                        ))
                        .check(status().is(204)))
                .exitHereIfFailed()
                .pause(1)
                .exec(http("GET /api/v1/incidence/#{createdId}")
                        .get("/api/v1/incidence/#{createdId}")
                        .check(status().is(200))
                        .check(jsonPath("$.description.name").is("Carga actualizado")))
                .exec(http("DELETE /api/v1/incidence")
                        .delete("/api/v1/incidence")
                        .asJson()
                        .body(StringBody(session ->
                                deleteBody(session.getString("createdId"))
                        ))
                        .check(status().is(204)));

        setUp(
                browse.injectOpen(
                        rampUsersPerSec(1).to(3).during(30),
                        constantUsersPerSec(3).during(60)
                ),
                readDetails.injectOpen(
                        rampUsersPerSec(1).to(3).during(30),
                        constantUsersPerSec(3).during(60)
                ),
                lifecycle.injectOpen(
                        rampUsersPerSec(0.1).to(0.5).during(30),
                        constantUsersPerSec(0.5).during(60)
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lt(1.0),
                        global().responseTime().percentile(95.0).lt(500)
                );
    }

    @Override
    public void before() {
        if (seedCount < 10) {
            throw new IllegalArgumentException("seedCount debe ser al menos 10");
        }

        var ids = new ArrayList<String>(seedCount);
        var mapper = JsonMapper.builder().build();

        try {
            // Usamos HttpClient nativo de Java para crear las incidencias
            var client = HttpClient.newHttpClient();

            for (int i = 0; i < seedCount; i++) {
                var request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/api/v1/incidence"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                createBody("Semilla-" + i)
                        ))
                        .build();

                var response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() != 201) {
                    throw new IllegalStateException(
                            "Error al preparar la incidencia " + i
                                    + ": HTTP " + response.statusCode()
                                    + ", respuesta: " + response.body()
                    );
                }

                String id = mapper.readValue(response.body(), String.class);
                UUID.fromString(id); // Valida que el cuerpo sea un UUID.
                ids.add(id);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo preparar la base de datos para la carga",
                    e
            );
        }

        seededIds = List.copyOf(ids);
        System.out.println("Preparadas " + seededIds.size()
                + " incidencias; comienza la carga medida.");
    }

    private static String createBody(String name) {
        // El timestamp y la duración usan representaciones ISO-8601.
        String timestamp = ZonedDateTime.now()
                .plusMinutes(5)
                .toOffsetDateTime()
                .toString();

        return """
                {
                  "affectedNetwork": {
                    "mapId": 1,
                    "lineIds": [2, 3]
                  },
                  "occurrence": {
                    "timestamp": "%s",
                    "duration": "PT5M"
                  },
                  "description": {
                    "name": "%s",
                    "summary": "Interrupción del servicio de prueba"
                  },
                  "classification": {
                    "severity": "MODERATE",
                    "cause": "WEATHER"
                  }
                }
                """.formatted(timestamp, name);
    }

    private static String updateBody(String id) {
        return """
                {
                  "uuid": "%s",
                  "changeDescription": {
                    "name": "Carga actualizada",
                    "summary": "Servicio restablecido"
                  }
                }
                """.formatted(id);
    }

    private static String deleteBody(String id) {
        return """
                {"id": "%s"}
                """.formatted(id);
    }
}

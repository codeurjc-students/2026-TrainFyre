package es.codeurjc.students.trainfyre.load;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * End-to-end load simulation for the TrainFyre REST API.
 *
 * The application must already be running. The simulation prepares a minimum set of
 * incidence records before traffic starts and removes only those records in after().
 * CRUD journeys create and delete their own temporary records.
 */
public final class TrainFyreLoadSimulation extends Simulation {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DEFAULT_BASE_URL = "https://localhost:443";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123456789";
    private static final String DEFAULT_NEW_USER_PASSWORD = "LoadTestUser1234!";

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    );
    private static final Pattern INCIDENCE_ID_PATTERN = Pattern.compile(
            "\"id\"\\s*:\\s*\"([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\""
    );
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\"accessToken\"\\s*:\\s*\"([^\"]+)\""
    );

    private final Config config = Config.fromSystemProperties();
    private volatile List<String> incidenceIds = List.of();
    private final List<String> createdSeedIncidenceIds = new CopyOnWriteArrayList<>();
    private String avatarFilePath = "";

    public TrainFyreLoadSimulation() {
        validateConfiguration();

        HttpProtocolBuilder httpProtocol = http
                .baseUrl(config.baseUrl())
                .acceptHeader(CONTENT_TYPE_JSON)
                .userAgentHeader("TrainFyre-load-test/1.0");

        ScenarioBuilder browseScenario = scenario("01 - Public incidence browsing")
                .repeat(3).on(
                        exec(session -> {
                            int[] pageSizes = {10, 20, 50, 300};
                            int pageSize = pageSizes[ThreadLocalRandom.current().nextInt(pageSizes.length)];
                            int pageCount = Math.max(1, (incidenceIds.size() + pageSize - 1) / pageSize);
                            int page = ThreadLocalRandom.current().nextInt(pageCount);
                            return session.set("pageSize", pageSize).set("page", page);
                        })
                        .exec(http("GET /api/v1/incidence")
                                .get("/api/v1/incidence")
                                .queryParam("page", session -> Integer.toString(session.getInt("page")))
                                .queryParam("size", session -> Integer.toString(session.getInt("pageSize")))
                                .check(status().is(200))
                                .check(jsonPath("$.content").exists()))
                        .pause(1, 3)
                );

        ScenarioBuilder loginScenario = scenario("02 - Login")
                .exec(adminLogin())
                .pause(1, 3);

        ScenarioBuilder profileScenario = scenario("03 - Authenticated user profile")
                .exec(adminLogin())
                .exec(http("GET /api/v1/users/me")
                        .get("/api/v1/users/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{accessToken}")
                        .check(status().is(200)))
                .pause(1, 3);

        ScenarioBuilder incidenceDetailScenario = scenario("04 - Incidence details")
                .exec(adminLogin())
                .exec(session -> session.set("incidenceId", randomIncidenceId()))
                .exec(http("GET /api/v1/incidence/{id}")
                        .get("/api/v1/incidence/#{incidenceId}")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{accessToken}")
                        .check(status().is(200)))
                .pause(1, 3);

        ScenarioBuilder incidenceCrudScenario = scenario("05 - Incidence create/read/update/delete")
                .exec(adminLogin())
                .exec(session -> session.set("incidenceName", "Load-" + UUID.randomUUID()))
                .exec(http("POST /api/v1/incidence")
                        .post("/api/v1/incidence")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{accessToken}")
                        .asJson()
                        .body(StringBody(session -> createIncidenceBody(session.getString("incidenceName"))))
                        .check(status().is(201))
                        .check(jsonPath("$").saveAs("createdIncidenceId")))
                .exitHereIfFailed()
                .pause(Duration.ofMillis(200))
                .exec(http("GET /api/v1/incidence/{id} [created]")
                        .get("/api/v1/incidence/#{createdIncidenceId}")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{accessToken}")
                        .check(status().is(200)))
                .pause(Duration.ofMillis(200))
                .exec(http("PUT /api/v1/incidence")
                        .put("/api/v1/incidence")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{accessToken}")
                        .asJson()
                        .body(StringBody(session -> updateIncidenceBody(session.getString("createdIncidenceId"))))
                        .check(status().is(204)))
                .pause(Duration.ofMillis(200))
                .exec(http("DELETE /api/v1/incidence")
                        .delete("/api/v1/incidence")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{accessToken}")
                        .asJson()
                        .body(StringBody(session -> deleteIncidenceBody(session.getString("createdIncidenceId"))))
                        .check(status().is(204)));

        ScenarioBuilder userLifecycleScenario = scenario("06 - User registration and self-management")
                .exec(session -> session.set("createdUsername", uniqueUsername()))
                .exec(http("POST /api/v1/users")
                        .post("/api/v1/users")
                        .asJson()
                        .body(StringBody(session -> createUserBody(
                                session.getString("createdUsername"), config.newUserPassword())))
                        .check(status().is(201))
                        .check(jsonPath("$").saveAs("createdUserId")))
                .exitHereIfFailed()
                .exec(http("POST /api/v1/auth/login [new user]")
                        .post("/api/v1/auth/login")
                        .asJson()
                        .body(StringBody(session -> loginBody(
                                session.getString("createdUsername"), config.newUserPassword())))
                        .check(status().is(200))
                        .check(jsonPath("$.accessToken").saveAs("userAccessToken")))
                .exitHereIfFailed()
                .pause(Duration.ofMillis(200))
                .exec(http("GET /api/v1/users/me [new user]")
                        .get("/api/v1/users/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .check(status().is(200)))
                .pause(Duration.ofMillis(200))
                .exec(http("PUT /api/v1/users/me")
                        .put("/api/v1/users/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .asJson()
                        .body(StringBody(session -> updateUserBody(session.getString("createdUsername"))))
                        .check(status().is(204)))
                .pause(Duration.ofMillis(200))
                .exec(http("DELETE /api/v1/users/{id} [owner]")
                        .delete("/api/v1/users/#{createdUserId}")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .check(status().is(204)));

        ScenarioBuilder avatarScenario = scenario("07 - Avatar upload/read/delete through Garage-backed S3 API")
                .exec(session -> session
                        .set("createdUsername", uniqueUsername())
                        .set("avatarFilePath", avatarFilePath))
                .exec(http("POST /api/v1/users [avatar user]")
                        .post("/api/v1/users")
                        .asJson()
                        .body(StringBody(session -> createUserBody(
                                session.getString("createdUsername"), config.newUserPassword())))
                        .check(status().is(201))
                        .check(jsonPath("$").saveAs("createdUserId")))
                .exitHereIfFailed()
                .exec(http("POST /api/v1/auth/login [avatar user]")
                        .post("/api/v1/auth/login")
                        .asJson()
                        .body(StringBody(session -> loginBody(
                                session.getString("createdUsername"), config.newUserPassword())))
                        .check(status().is(200))
                        .check(jsonPath("$.accessToken").saveAs("userAccessToken")))
                .exitHereIfFailed()
                .exec(http("PUT /api/v1/avatars/me [multipart file]")
                        .put("/api/v1/avatars/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .formUpload("file", session -> session.getString("avatarFilePath"))
                        .check(status().is(204)))
                .exec(http("GET /api/v1/avatars/me")
                        .get("/api/v1/avatars/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .check(status().is(200)))
                .exec(http("DELETE /api/v1/avatars/me")
                        .delete("/api/v1/avatars/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .check(status().is(204)))
                .exec(http("DELETE /api/v1/users/{id} [avatar owner]")
                        .delete("/api/v1/users/#{createdUserId}")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + "#{userAccessToken}")
                        .check(status().is(204)));

        List<PopulationBuilder> populations = new ArrayList<>();
        addPopulation(populations, browseScenario, config.browseRate());
        addPopulation(populations, loginScenario, config.loginRate());
        addPopulation(populations, profileScenario, config.profileRate());
        addPopulation(populations, incidenceDetailScenario, config.detailRate());
        addPopulation(populations, incidenceCrudScenario, config.incidenceCrudRate());
        addPopulation(populations, userLifecycleScenario, config.userLifecycleRate());
        if (config.includeAvatarTests()) {
            addPopulation(populations, avatarScenario, config.avatarRate());
        }

        setUp(populations.toArray(PopulationBuilder[]::new))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lt(config.maxFailedPercent()),
                        global().responseTime().percentile(95.0).lt(config.maxP95Millis()),
                        global().responseTime().percentile(99.0).lt(config.maxP99Millis())
                );
    }

    private void addPopulation(
            List<PopulationBuilder> populations,
            ScenarioBuilder scenario,
            double rate
    ) {
        if ("smoke".equals(config.profile())) {
            populations.add(scenario.injectOpen(atOnceUsers(1)));
        } else if (rate > 0) {
            populations.add(inject(scenario, rate));
        }
    }

    private PopulationBuilder inject(ScenarioBuilder scenario, double rate) {
        return switch (config.profile()) {
            case "realistic" -> scenario.injectOpen(
                    rampUsersPerSec(rate * 0.25).to(rate).during(config.rampUpSeconds()),
                    constantUsersPerSec(rate).during(config.durationSeconds())
            );
            case "stress" -> scenario.injectOpen(
                    rampUsersPerSec(rate * 0.25).to(rate).during(config.stageSeconds()),
                    rampUsersPerSec(rate).to(rate * 2).during(config.stageSeconds()),
                    rampUsersPerSec(rate * 2).to(rate * 4).during(config.stageSeconds())
            );
            case "spike" -> scenario.injectOpen(
                    constantUsersPerSec(rate).during(45),
                    rampUsersPerSec(rate).to(rate * 5).during(15),
                    rampUsersPerSec(rate * 5).to(rate).during(60)
            );
            case "soak" -> scenario.injectOpen(
                    constantUsersPerSec(rate).during(config.soakDurationSeconds())
            );
            default -> throw new IllegalStateException("Unsupported load profile: " + config.profile());
        };
    }

    private ChainBuilder adminLogin() {
        return exec(http("POST /api/v1/auth/login")
                .post("/api/v1/auth/login")
                .asJson()
                .body(StringBody(loginBody(config.adminUsername(), config.adminPassword())))
                .check(status().is(200))
                .check(jsonPath("$.accessToken").saveAs("accessToken")))
                .exitHereIfFailed();
    }

    @Override
    public void before() {
        try (HttpClient client = createSetupHttpClient()) {
            String token = loginAndGetToken(client, config.adminUsername(), config.adminPassword());
            List<String> ids = readIncidenceIds(client);
            int targetCount = Math.max(10, config.seedIncidenceCount());

            while (ids.size() < targetCount) {
                String id = createSeedIncidence(client, token, ids.size());
                ids.add(id);
                createdSeedIncidenceIds.add(id);
            }

            verifyAdminAccess(client, token, ids.getFirst());
            incidenceIds = List.copyOf(ids);

            if (config.includeAvatarTests()) {
                avatarFilePath = createTemporaryAvatarFile();
            }

            System.out.printf(
                    "TrainFyre load setup: profile=%s, baseUrl=%s, incidences=%d, garageAvatarFlow=%s%n",
                    config.profile(), config.baseUrl(), incidenceIds.size(), config.includeAvatarTests()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Load test preparation was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not prepare the TrainFyre load test", exception);
        }
    }

    @Override
    public void after() {
        try (HttpClient client = createSetupHttpClient()) {
            String token = loginAndGetToken(client, config.adminUsername(), config.adminPassword());
            for (String id : createdSeedIncidenceIds) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.baseUrl() + "/api/v1/incidence"))
                        .header("Content-Type", CONTENT_TYPE_JSON)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                        .method("DELETE", HttpRequest.BodyPublishers.ofString(deleteIncidenceBody(id)))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 204) {
                    System.err.printf(
                            "Could not remove setup incidence %s: HTTP %d%n", id, response.statusCode()
                    );
                }
            }
        } catch (Exception exception) {
            System.err.println("Warning: load-test cleanup failed: " + exception.getMessage());
        } finally {
            if (!avatarFilePath.isBlank()) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(avatarFilePath));
                } catch (IOException exception) {
                    System.err.println("Warning: temporary avatar cleanup failed: " + exception.getMessage());
                }
            }
        }
    }

    private void validateConfiguration() {
        if (!Set.of("smoke", "realistic", "stress", "spike", "soak").contains(config.profile())) {
            throw new IllegalArgumentException(
                    "loadProfile must be one of: smoke, realistic, stress, spike, soak"
            );
        }
        if (config.loadFactor() <= 0
                || config.rampUpSeconds() <= 0
                || config.durationSeconds() <= 0
                || config.stageSeconds() <= 0
                || config.soakDurationSeconds() <= 0
                || config.seedIncidenceCount() <= 0) {
            throw new IllegalArgumentException("Load factor, durations and seed count must be positive");
        }
        if (config.maxFailedPercent() < 0
                || config.maxFailedPercent() > 100
                || config.maxP95Millis() <= 0
                || config.maxP99Millis() <= 0) {
            throw new IllegalArgumentException("Assertion thresholds are outside their valid ranges");
        }
    }

    private String randomIncidenceId() {
        return incidenceIds.get(ThreadLocalRandom.current().nextInt(incidenceIds.size()));
    }

    private List<String> readIncidenceIds(HttpClient client) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl() + "/api/v1/incidence?page=0&size=300"))
                .header("Accept", CONTENT_TYPE_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "GET /api/v1/incidence failed: HTTP " + response.statusCode() + " - " + response.body()
            );
        }

        Matcher matcher = INCIDENCE_ID_PATTERN.matcher(response.body());
        List<String> ids = new ArrayList<>();
        while (matcher.find()) {
            String id = matcher.group(1);
            if (!ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private String createSeedIncidence(HttpClient client, String token, int index)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl() + "/api/v1/incidence"))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .POST(HttpRequest.BodyPublishers.ofString(createIncidenceBody("Load-seed-" + index)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Could not create setup incidence " + index + ": HTTP " + response.statusCode()
                            + " - " + response.body()
            );
        }
        String id = extractUuid(response.body());
        if (id == null) {
            throw new IllegalStateException("Create incidence response did not contain a UUID");
        }
        return id;
    }

    private void verifyAdminAccess(HttpClient client, String token, String id)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl() + "/api/v1/incidence/" + id))
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "The configured account can log in but cannot read incidence details (HTTP "
                            + response.statusCode() + "). Use an ADMIN account."
            );
        }
    }

    private String loginAndGetToken(HttpClient client, String username, String password)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl() + "/api/v1/auth/login"))
                .header("Content-Type", CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(loginBody(username, password)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Login failed for " + username + ": HTTP " + response.statusCode() + " - " + response.body()
            );
        }
        Matcher matcher = TOKEN_PATTERN.matcher(response.body());
        if (!matcher.find()) {
            throw new IllegalStateException("Login response did not contain accessToken");
        }
        return matcher.group(1);
    }

    private static HttpClient createSetupHttpClient() throws Exception {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // The setup client does not authenticate as a TLS client; this hook is intentionally unused.
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // This client is restricted to test environments and must accept the local self-signed certificate.
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private static String createTemporaryAvatarFile() throws IOException {
        // Use a realistic image payload instead of a tiny placeholder so Garage writes are measurable.
        int width = 256;
        int height = 256;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        SplittableRandom random = new SplittableRandom(0x545241494e465952L);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = random.nextInt(256);
                int green = random.nextInt(256);
                int blue = random.nextInt(256);
                image.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }

        java.nio.file.Path path = java.nio.file.Files.createTempFile("trainfyre-load-avatar-", ".png");
        if (!ImageIO.write(image, "png", path.toFile())) {
            java.nio.file.Files.deleteIfExists(path);
            throw new IOException("No PNG writer is available to create the avatar test payload");
        }
        path.toFile().deleteOnExit();
        return path.toAbsolutePath().toString();
    }

    private static String uniqueUsername() {
        return "lt-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private static String createUserBody(String username, String password) {
        return "{\"username\":\"" + jsonEscape(username) + "\",\"password\":\""
                + jsonEscape(password) + "\"}";
    }

    private static String loginBody(String username, String password) {
        return "{\"username\":\"" + jsonEscape(username) + "\",\"password\":\""
                + jsonEscape(password) + "\"}";
    }

    private static String updateUserBody(String username) {
        return "{\"username\":\"" + jsonEscape(username + "u") + "\"}";
    }

    private static String createIncidenceBody(String name) {
        String timestamp = OffsetDateTime.now().plusMinutes(5).toString();
        return "{"
                + "\"affectedNetwork\":{\"mapId\":1,\"lineIds\":[2,3]},"
                + "\"occurrence\":{\"timestamp\":\"" + timestamp + "\",\"duration\":\"PT5M\"},"
                + "\"description\":{\"name\":\"" + jsonEscape(name)
                + "\",\"summary\":\"Service interruption created by load test\"},"
                + "\"classification\":{\"severity\":\"MODERATE\",\"cause\":\"WEATHER\"}"
                + "}";
    }

    private static String updateIncidenceBody(String id) {
        return "{\"uuid\":\"" + id + "\",\"changeDescription\":{"
                + "\"name\":\"Load updated\",\"summary\":\"Updated by load test\"}}";
    }

    private static String deleteIncidenceBody(String id) {
        return "{\"id\":\"" + id + "\"}";
    }

    private static String extractUuid(String body) {
        Matcher matcher = UUID_PATTERN.matcher(body);
        return matcher.find() ? matcher.group() : null;
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record Config(
            String baseUrl,
            String adminUsername,
            String adminPassword,
            String newUserPassword,
            String profile,
            double loadFactor,
            int rampUpSeconds,
            int durationSeconds,
            int stageSeconds,
            int soakDurationSeconds,
            int seedIncidenceCount,
            boolean includeAvatarTests,
            double browseRate,
            double detailRate,
            double profileRate,
            double loginRate,
            double incidenceCrudRate,
            double userLifecycleRate,
            double avatarRate,
            double maxFailedPercent,
            int maxP95Millis,
            int maxP99Millis
    ) {
        private static Config fromSystemProperties() {
            double loadFactor = doubleProperty("loadFactor", 1.0);
            return new Config(
                    System.getProperty("baseUrl", DEFAULT_BASE_URL).replaceAll("/+$", ""),
                    System.getProperty("loadAdminUsername", DEFAULT_ADMIN_USERNAME),
                    System.getProperty("loadAdminPassword", DEFAULT_ADMIN_PASSWORD),
                    System.getProperty("loadNewUserPassword", DEFAULT_NEW_USER_PASSWORD),
                    System.getProperty("loadProfile", "realistic").toLowerCase(Locale.ROOT),
                    loadFactor,
                    intProperty("rampUpSeconds", 60),
                    intProperty("durationSeconds", 180),
                    intProperty("stageSeconds", 60),
                    intProperty("soakDurationSeconds", 1200),
                    intProperty("seedIncidenceCount", 100),
                    Boolean.parseBoolean(System.getProperty("includeAvatarTests", "true")),
                    doubleProperty("browseRate", 8.0) * loadFactor,
                    doubleProperty("detailRate", 2.0) * loadFactor,
                    doubleProperty("profileRate", 1.0) * loadFactor,
                    doubleProperty("loginRate", 0.5) * loadFactor,
                    doubleProperty("incidenceCrudRate", 0.25) * loadFactor,
                    doubleProperty("userLifecycleRate", 0.10) * loadFactor,
                    doubleProperty("avatarRate", 0.02) * loadFactor,
                    doubleProperty("maxFailedPercent", 2.0),
                    intProperty("maxP95Millis", 1000),
                    intProperty("maxP99Millis", 2000)
            );
        }

        private static int intProperty(String name, int fallback) {
            return Integer.getInteger(name, fallback);
        }

        private static double doubleProperty(String name, double fallback) {
            return Double.parseDouble(System.getProperty(name, Double.toString(fallback)));
        }
    }
}

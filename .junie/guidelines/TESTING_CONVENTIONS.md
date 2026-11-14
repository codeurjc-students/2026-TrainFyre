# Testing Conventions — TrainFyre

> Resumen rápido de las convenciones de testing del proyecto. Úsalo como referencia al crear o revisar tests.

## 📁 Estructura de Paquetes

Los tests mirroran la estructura `main` con subpaquetes por capa:

```
src/test/java/es/codeurjc/students/trainfyre/
├── statistics/
│   ├── domain/                    # IncidenceTest, ClassificationTest, ...
│   ├── application/
│   │   └── service/               # CreateIncidenceServiceTest, ...
│   ├── infrastructure/
│   │   └── repository/            # IncidenceRepositoryIntegrationTest, ...
│   └── integration/               # Flujos completos con DB
│       └── e2e/                   # Rest Assured
├── StatisticsTestBuilder.java     # Builder compartido
├── users/
│   ├── domain/                    # UserTest, AlertSubscriptionTest, ...
│   └── application/
├── images/
├── security/
└── ArchitectureTest.java          # ArchUnit + jMolecules
```

## 🏷️ Naming Conventions

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Clase de test | `{ClassName}Test` (package-private) | `class IncidenceTest` |
| Método de test | `should[ExpectedBehavior]When[Condition]` | `shouldRejectNullArgumentsWhenChanged` |
| TestBuilder | `{Entity}TestBuilder` | `IncidenceTestBuilder` |
| Método factory | `an{Entity}()` | `anIncidence()` |
| Data provider | `{scenario}()` (static Stream) | `invalidNullArguments()` |

## 🧪 Patrones por Capa

### Domain Tests — Tests Puros
- **Sin mocks**, sin Spring, sin dependencias externas.
- Usa **TestBuilders** para crear entidades.
- **Un requisito = un test**: agrupa los casos del mismo requisito en un `@ParameterizedTest`.
  - *Ejemplo*: "no argumentos nulos" → un `@ParameterizedTest` con todos los campos nulos.
  - *Ejemplo*: "validar cambios null" → un `@ParameterizedTest` con todos los campos.
- Cubre: constructores, invariantes, cambios de estado, value objects.

```java
class IncidenceTest {

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(IllegalArgumentException.class, creation);
    }

    static Stream<Executable> invalidNullArguments() {
        return Stream.of(
            () -> anIncidence().withAffectedNetwork(null).build(),
            () -> anIncidence().withDescription(null).build()
        );
    }

    @Test
    void shouldAllowChangingDescription() {
        Incidence incidence = anIncidence().build();
        Description newDesc = new Description("Title", "Body");
        incidence.changeDescription(newDesc);
        assertThat(incidence.getDescription()).isEqualTo(newDesc);
    }
}
```

### Application Tests — Mockito
- **@ExtendWith(MockitoExtension.class)** para inyección de mocks.
- Mock de **puertos de salida** (IncidencePort, UserRepository, etc.).
- **ArgumentCaptor** para verificar lo que se pasa a los puertos.
- Un test por caso de uso.

```java
@ExtendWith(MockitoExtension.class)
class CreateIncidenceServiceTest {

    @Mock
    private IncidencePort incidencePort;
    private CreateIncidenceUseCase createIncidenceUseCase;

    @BeforeEach
    void setUp() {
        createIncidenceUseCase = new CreateIncidenceService(incidencePort);
    }

    @Test
    void shouldCreateAndSaveAnIncidence() {
        UUID uuid = createIncidenceUseCase.execute(command);
        ArgumentCaptor<Incidence> captor = ArgumentCaptor.forClass(Incidence.class);
        verify(incidencePort).save(captor.capture());
        assertThat(uuid).isEqualTo(captor.getValue().getId());
    }
}
```

### Integration Tests — Testcontainers
- **@Testcontainers** + **@Container** para PostgreSQL/MariaDB.
- **@AutoConfigureTestDatabase(replace = Replace.NONE)** para usar DB real.
- Verifica: CRUD completo, constraints, transacciones.

```java
@Testcontainers
class IncidenceRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private IncidenceRepository repository;

    @Test
    void shouldSaveAndFindIncidence() {
        Incidence incidence = anIncidence().build();
        Incidence saved = repository.save(incidence);
        Optional<Incidence> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
    }
}
```

### E2E Tests — Rest Assured
- Peticiones HTTP reales contra la aplicación.
- Verifica: status codes, response body, headers.

```java
class IncidencesE2ETest {

    @Test
    void shouldReturnIncidences() {
        given()
            .when().get("/api/incidences")
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
}
```

## 🏗️ TestBuilders

### Reglas
1. **Valores por defecto realistas** — no `null`, no valores vacíos.
2. **Método estático factory** — `an{Entity}()` para fluencia.
3. **Métodos `with{Field}()`** — para sobrescribir campos.
4. **Método `build()`** — crea la entidad usando el factory method del dominio.
5. **Fábricas estáticas** — `generateDefault{Field}()` para value objects comunes.

```java
public class IncidenceTestBuilder {

    private AffectedNetwork affectedNetwork = new AffectedNetwork(1L, List.of(1L, 2L, 3L));
    private Occurrence occurrence = new Occurrence(
        ZonedDateTime.parse("2026-01-01T10:00:00Z"), Duration.ofMinutes(30));
    private Description description = new Description("Train failure", "Engine broken");
    private Classification classification = new Classification(Severity.MODERATE, Cause.MEDICAL_EMERGENCY);

    public static IncidenceTestBuilder anIncidence() {
        return new IncidenceTestBuilder();
    }

    public IncidenceTestBuilder withAffectedNetwork(AffectedNetwork network) {
        this.affectedNetwork = network;
        return this;
    }

    public Incidence build() {
        return Incidence.createIncidence(affectedNetwork, occurrence, description, classification);
    }

    public static Description generateDefaultDescription() {
        return new Description("Train failure", "Engine broken");
    }
}
```

## ✅ Checklist de Calidad

Antes de considerar un test completo, verifica:

- [ ] **Nombre descriptivo**: el nombre del test explica qué se prueba.
- [ ] **Arrange-Act-Assert**: estructura clara en tres fases.
- [ ] **Independiente**: el test puede correr solo, sin orden específico.
- [ ] **Determinista**: mismo resultado cada vez que se ejecuta.
- [ ] **Cubre happy path**: caso exitoso con datos válidos.
- [ ] **Cubre casos de borde**: null, vacíos, límites.
- [ ] **Cubre invariantes**: reglas de negocio que no se deben violar.
- [ ] **Usa TestBuilder**: si la entidad tiene 3+ parámetros.
- [ ] **Usa parametrización**: si hay 3+ casos similares.
- [ ] **Assertions significativas**: verifica el comportamiento, no la implementación.

## 🛠️ Herramientas

| Herramienta | Propósito |
|------------|-----------|
| **JUnit 5** | Framework de tests |
| **AssertJ** | Assertions fluidas (`assertThat`) |
| **Mockito** | Mocks y stubs (`@Mock`, `when`, `verify`) |
| **Testcontainers** | Contenedores Docker para DB reales |
| **Rest Assured** | Tests HTTP E2E |
| **ArchUnit** | Tests de arquitectura |
| **jMolecules** | Validación de patrones DDD/hexagonal |
| **JaCoCo** | Cobertura de código |
| **Gatling** | Tests de carga |

## 📊 Cobertura Esperada

| Capa | Cobertura mínima |
|------|-----------------|
| Domain | 95%+ |
| Application | 90%+ |
| Infrastructure | 80%+ |
| Controllers | 70%+ |

## 🔄 Comandos

```bash
# Unitarios solo
mvn test -Dtest='!*IntegrationTest,!*E2ETest'

# Todo el suite
mvn verify

# Test específico
mvn test -Dtest='IncidenceTest'

# Con cobertura
mvn verify -DskipTests=false
```

## 🖥️ Frontend — Enfoque Diferente

El frontend **no sigue TDD estricto** como el backend. El enfoque es:

1. El usuario proporciona la **API Swagger / documentación** de los endpoints.
2. La IA genera o refactoriza componentes, hooks, y páginas con **libertad creativa**.
3. Los tests (Vitest, Playwright) se generan como **complemento**, no como requisito previo.

### Comandos Frontend

```bash
# Tests unitarios con Vitest
cd frontend && npm run test:unit

# Cobertura V8
cd frontend && npm run test:coverage

# Tests de integración
cd frontend && npm run test:integration

# Tests E2E con Playwright
cd frontend && npm run test:e2e
```

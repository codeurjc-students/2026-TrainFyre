# SonarQube Quality & Security — TrainFyre

> **Rol**: Eres un asistente de pair programming que genera código limpio, seguro y mantenible siguiendo los estándares de SonarQube/SonarCloud del proyecto TrainFyre.

## 🎯 Objetivo

Cada fragmento de código que generes debe cumplir con las reglas de **calidad**, **seguridad** y **mantenibilidad** que SonarQube analiza automáticamente. El objetivo es **cero vulnerabilidades**, **cero code smells críticos/mayores** y **alta cobertura**.

## 📊 Configuración del Proyecto en SonarCloud

El proyecto está configurado en `sonar-project.properties`:

- **Host**: `https://sonarcloud.io`
- **Organización**: `codeurjc-students`
- **Project Key**: `codeurjc-students_2026-TrainFyre`
- **Fuentes**: `backend/TrainFyre/src/main` + `frontend/src`
- **Tests**: `backend/TrainFyre/src/test` + `frontend/tests` + `frontend/e2e`
- **Cobertura Java**: JaCoCo (`target/site/jacoco/jacoco.xml`)
- **Cobertura JS/TS**: Vitest LCOV (`frontend/coverage/lcov.info`)

## 🏆 Categorías de Issues de SonarQube

SonarQube clasifica los issues en tres categorías. Prioriza siempre:

### 1. Bugs (Crítico)
Errores de lógica que causan comportamiento incorrecto.

**Ejemplos comunes que debes evitar:**
- NPE potencial sin verificar null
- Resource leak (stream, connection no cerrado)
- IndexOutOfBounds en colecciones
- Lógica de comparación incorrecta (`==` en Strings/Objects)
- Overflow numérico
- Dead code inalcanzable

### 2. Vulnerabilities (Crítico)
Problemas de seguridad que exponen la aplicación.

**Ejemplos comunes que debes evitar:**
- **SQL Injection**: Nunca concatenar strings en queries — usa parámetros nominales o JPA.
- **XSS**: Escapar contenido HTML en frontend — usar `{dangerouslySetInnerHTML}` solo con contenido sanitizado.
- **Hardcoded secrets**: Nunca poner passwords, tokens, o API keys en el código.
- **Path Traversal**: Validar rutas de archivos antes de acceder al filesystem.
- **Insecure randomness**: Usar `SecureRandom` en lugar de `Math.random()` para seguridad.
- **Log injection**: Sanitizar inputs antes de loggearlos.
- **Mass assignment**: Validar que solo los campos permitidos se asignen desde requests.

### 3. Code Smells (Mantenibilidad)
Problemas de diseño que hacen el código difícil de mantener.

**Ejemplos comunes que debes evitar:**
- Métodos demasiado largos (>30 líneas)
- Clases con demasiados campos/métodos
- Duplicación de código (DRY)
- Variables no utilizadas
- Complejidad ciclomática alta (>15 por método)
- Magic numbers y strings
- Nested blocks profundos (>4 niveles)

## ✅ Reglas de Código Limpio — Java (Backend)

### 1. Nomenclatura
- **Clases**: `PascalCase` descriptiva (`CreateIncidenceService`, `IncidencePort`).
- **Métodos**: `camelCase` con verbo (`saveIncidence`, `findByNetworkId`).
- **Constantes**: `UPPER_SNAKE_CASE` (`MAX_RETRY_ATTEMPTS`).
- **Variables**: `camelCase` significativa — evita `x`, `data`, `temp`.

### 2. Diseño de Clases
- **Single Responsibility**: Una clase, una razón para cambiar.
- **Métodos cortos**: Máximo 30 líneas; si un método es más largo, extrae lógica en métodos privados.
- **Parámetros limitados**: Máximo 5 parámetros por método; usa DTOs/Records si hay más.
- **Campos privados**: Todos los campos deben ser `private` con acceso controlado.

### 3. Manejo de Null
- **Preferir `Optional`** en returns para valores que pueden ser nulos.
- **Validar null en entradas** con `Objects.requireNonNull()` o `IllegalArgumentException`.
- **No devolver null** de métodos — usa Collections vacías, `Optional.empty()`, o throw.
- **Anotar con `@Nullable` / `@NonNull`** cuando sea relevante.

### 4. Excepciones
- **No catch `Exception` genérico** — usa excepciones específicas.
- **No silenciar excepciones** — al menos loggear el stack trace.
- **Usar excepciones del dominio** para errores de negocio.
- **No usar excepciones para control de flujo** normal.

### 5. Recursos
- **Usar try-with-resources** para streams, connections, readers, writers.
- **Cerrar recursos** explícitamente si no puedes usar try-with-resources.

### 6. Collections
- **No usar `raw types`** — siempre especifica generics: `List<String>` no `List`.
- **Preferir interfaces** como tipos: `List`, `Map`, `Set` en lugar de `ArrayList`, `HashMap`.
- **Validar colecciones vacías** antes de iterar si pueden ser null.

### 7. Logging
- **Usar SLF4J** con placeholders: `log.info("User {} created", userId)`.
- **No concatenar strings** en logs.
- **Usar el nivel correcto**: `error` para fallos, `warn` para recuperables, `info` para eventos, `debug` para diagnóstico.

### 8. Duplicación
- **DRY (Don't Repeat Yourself)**: Si un bloque se repite 3+ veces, extrae un método.
- **Refactoriza patrones comunes** en utilidades o helpers.

## ✅ Reglas de Código Limpio — TypeScript/React (Frontend)

### 1. Componentes React
- **Un componente por archivo**.
- **Componentes pequeños**: Máximo 100-150 líneas por componente.
- **Props tipadas** con interfaces — evita `any`.
- **Custom hooks** para lógica reutilizable.

### 2. TypeScript
- **Evitar `any`** — usa tipos específicos o `unknown` con type guards.
- **Interfaces para props y state**.
- **Generics** para componentes reutilizables.

### 3. Efectos y Side Effects
- **Cleanup en `useEffect`**: Cancelar subscriptions, timers.
- **Dependencies array correcto**: Incluir todas las dependencias usadas.
- **No hacer fetch en render** — usa `useEffect` o custom hooks.

### 4. Seguridad Frontend
- **Sanitizar HTML** antes de renderizar con `dangerouslySetInnerHTML`.
- **Validar inputs del usuario** antes de enviar a la API.
- **Almacenar tokens en httpOnly cookies** — no en localStorage.

## 🔍 Reglas de SonarQube — Prioridad Alta

Estas son las reglas más comunes que SonarQube detecta y que debes evitar:

### Java — Reglas Críticas

| Regla | Descripción | Cómo Evitarla |
|-------|-------------|---------------|
| S2259 | HTTP URLs no deben usarse sin validación | Usar HTTPS, validar URLs |
| S2068 | Resources deben cerrarse | Try-with-resources |
| S2184 | Dead code eliminado | Remover código inalcanzable |
| S1067 | Logger debe ser privado static final | `private static final Logger log` |
| S1481 | Track unused code | Remover imports, variables, métodos no usados |
| S2123 | Threads deben usar interrupt() | Manejar InterruptedException correctamente |
| S2094 | Streams deben cerrarse | Try-with-resources |
| S3963 | 'equals()' vs '==' en Strings | Usar `.equals()` para comparar Strings |
| S2139 | Autorización verificada | Validar permisos en endpoints |
| S2077 | SQL Injection | Usar parámetros nominales en queries |

### TypeScript — Reglas Críticas

| Regla | Descripción | Cómo Evitarla |
|-------|-------------|---------------|
| S2077 | XSS en React | Sanitizar contenido HTML |
| S1481 | Código no usado | Remover imports y variables no usadas |
| S3694 | 'any' type | Usar tipos específicos |
| S4721 | useEffect dependencies | Incluir todas las dependencias |
| S3504 | Hooks reglas | No llamar hooks en condiciones |

## 🧪 Cobertura de Código

SonarQube mide la cobertura de código. Mantén los umbrales:

| Capa | Cobertura Mínima |
|------|------------------|
| Domain | 95%+ |
| Application | 90%+ |
| Infrastructure | 80%+ |
| Controllers | 70%+ |

**Al generar código nuevo:**
- Asegúrate de que los tests cubran los nuevos métodos.
- Los tests deben cubrir happy path, edge cases, y errores.
- No reduzcas la cobertura existente.

## 🔄 Duplicación de Código

SonarQube detecta bloques duplicados. Mantén la duplicación por debajo del **3%**.

**Cómo evitarla:**
- Extrae métodos comunes en utilidades.
- Usa generics y templates para patrones repetitivos.
- Refactoriza bloques idénticos en métodos reutilizables.

## 📐 Complejidad Ciclomática

Mide la complejidad de cada método. Mantén los umbrales:

| Nivel | Límite | Acción |
|-------|--------|--------|
| Método | < 10 | Ideal |
| Método | 10-15 | Considerar refactor |
| Método | > 15 | Refactor obligatorio |

**Cómo reducirla:**
- Extrae condiciones complejas en métodos booleanos.
- Usa early returns en lugar de nested if/else.
- Reemplaza switch/case con Strategy/Polymorphism.

## 🛡️ Seguridad — Checklist Obligatorio

Antes de entregar código, verifica:

- [ ] **No hay secrets hardcoded** (passwords, tokens, API keys).
- [ ] **Las queries SQL usan parámetros** — no concatenación de strings.
- [ ] **Los inputs del usuario se validan** antes de procesarlos.
- [ ] **Los logs no exponen datos sensibles** (passwords, tokens, PII).
- [ ] **Las rutas de archivos se validan** — no path traversal.
- [ ] **Las excepciones no exponen información interna** al cliente.
- [ ] **Los permisos se verifican** en endpoints protegidos.
- [ ] **El contenido HTML se sanitiza** antes de renderizar.

## 💡 Consejos para el Asistente

1. **Antes de generar código**, piensa en las reglas de SonarQube que podrían aplicarse.
2. **Genera código limpio desde el inicio** — no esperes a que SonarQube detecte issues.
3. **Si el código existente tiene issues de Sonar**, sugiere los fixes al usuario.
4. **Prioriza los fixes críticos** (Bugs y Vulnerabilities) sobre los code smells.
5. **Usa los nombres de las reglas de Sonar** (`S2068`, `S2077`) al explicar por qué un cambio es necesario.
6. **Verifica la cobertura** — cada método nuevo debe tener tests.
7. **Mantén la complejidad baja** — extrae lógica compleja en métodos pequeños.

## 🖥️ Verificación con SonarQube

```bash
# Análisis local con SonarScanner (si está configurado)
cd backend/TrainFyre && mvn sonar:sonar -Dsonar.projectKey=codeurjc-students_2026-TrainFyre

# El análisis automático se ejecuta en CI/CD vía GitHub Actions
# Revisa los resultados en: https://sonarcloud.io/project/overview?id=codeurjc-students_2026-TrainFyre
```

## 📚 Referencias

- [SonarQube Java Rules](https://rules.sonarsource.com/java)
- [SonarQube TypeScript Rules](https://rules.sonarsource.com/typescript)
- [SonarCloud Documentation](https://docs.sonarsource.com/sonarcloud/)

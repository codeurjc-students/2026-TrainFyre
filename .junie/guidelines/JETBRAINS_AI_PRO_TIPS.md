# JetBrains AI (Air) — Pro Tips para TrainFyre

## 🚀 Productividad con AI en IntelliJ IDEA

### 1. Contexto es Rey
- **Abre los archivos relevantes** antes de hacer preguntas — AI lee el contexto del editor.
- **Selecciona código** antes de pedir ayuda para que AI entienda exactamente qué bloque te interesa.
- **Usa tabs abiertos** a tu favor: AI considera los archivos visibles como contexto activo.

### 2. Prompts Efectivos

#### ❌ Malo
```
"ayúdame con los tests"
```

#### ✅ Bueno
```
"Crea los tests de dominio para la clase User siguiendo TDD.
Cubre: creación con credenciales nulas, suscripción a alertas, 
cambio de username y password. Usa el patrón TestBuilder y 
tests parametrizados como en IncidenceTest."
```

#### ✅ Excelente
```
"Quiero implementar la validación de suscripciones a alertas en User.
Requisitos:
- Una suscripción no puede tener fecha fin antes de fecha inicio
- Los días de la semana no pueden estar vacíos
- Las líneas afectadas deben ser al menos 1
- No se pueden duplicar suscripciones iguales

Crea los tests primero (TDD), usando:
- @ParameterizedTest para los casos de validación
- TestBuilder para AlertSubscription
- AssertJ para assertions
- Nombres should... como en UserTest.java existente"
```

### 3. Atajos de Teclado Esenciales

| Acción | Windows/Linux | macOS |
|--------|--------------|-------|
| Invocar AI Chat | `Ctrl+J` | `Cmd+J` |
| Completar código (Tab) | `Ctrl+Shift+Enter` | `Cmd+Shift+Enter` |
| Explicar código seleccionado | `Ctrl+J` → "Explain" | `Cmd+J` → "Explain" |
| Generar tests | `Ctrl+J` → "Generate tests" | `Cmd+J` → "Generate tests" |
| Refactorizar selección | `Ctrl+J` → "Refactor" | `Cmd+J` → "Refactor" |
| Inline documentation | `Ctrl+J` → "Document" | `Cmd+J` → "Document" |

### 4. Flujos de Trabajo Avanzados

#### A. TDD con AI
```
1. Describe el requisito en el chat
2. AI genera los tests (los copias)
3. Compila → tests rojos ✅
4. Implementa el código
5. Tests verdes ✅
6. Pide a AI: "Revisa estos tests y sugiere refactorizaciones"
```

#### B. Code Review con AI
```
1. Selecciona el bloque de código
2. Ctrl+J → "Review this code"
3. AI sugiere mejoras de:
   - Legibilidad
   - Performance
   - Seguridad
   - Convenciones del proyecto
```

#### C. Generar Documentación
```
1. Abre la clase
2. Ctrl+J → "Generate KDoc for this class"
3. AI crea documentación basada en:
   - Parámetros
   - Return types
   - Excepciones
   - Comportamiento observable
```

#### D. Debugging Asistido
```
1. Pega el error/stack trace en el chat
2. AI analiza y sugiere:
   - Causa raíz
   - Archivos afectados
   - Posibles fixes
3. Verifica con breakpoints y debugger
```

### 5. Guías y Contexto Personalizado

#### Archivos de Guidelines (`.junie/guidelines/`)
AI lee automáticamente los archivos en `.junie/guidelines/` para entender:
- Convenciones del proyecto
- Patrones de código esperados
- Reglas de arquitectura
- Estrategia de testing

**Guías disponibles en este proyecto:**
- `TDD_PAIR_PROGRAMMING.md` — Flujo TDD y patrones de testing
- `TESTING_CONVENTIONS.md` — Convenciones específicas de tests
- `SONARQUBE_QUALITY.md` — Estándares de calidad, seguridad y código limpio de SonarQube

#### Cómo Aprovecharlas
- Las guías se cargan como contexto en cada sesión
- AI sigue las convenciones definidas automáticamente
- Puedes añadir nuevas guías para dominios específicos

### 6. Sub-Agentes Especializados

El proyecto tiene skills disponibles para tareas específicas:

| Skill | Cuándo Usarlo |
|-------|--------------|
| `database-connection-management` | Configurar datasources en DataGrip/IDEA |
| `ij-debugger` | Debugging JVM con logpoints y breakpoints |
| `demo-setup` | Configurar entorno demo con Docker |
| `session-history` | Revisar sesiones anteriores de trabajo |

### 7. Tips de Arquitectura Hexagonal

#### Cuando Pidas Código
```
"Crea el use case para [acción] siguiendo arquitectura hexagonal:
- Puerto de entrada (interface)
- Servicio de aplicación (implementación)
- Puerto de salida (interface)
- No toques la infraestructura"
```

#### Cuando Pidas Tests
```
"Los tests deben respetar las capas:
- Domain: tests puros, sin mocks
- Application: Mockito para puertos de salida
- Infrastructure: Testcontainers para DB real"
```

### 8. Evitar Errores Comunes

#### ❌ No Hacer
- Pedir cambios sin contexto del archivo
- Esperar que AI adivine convenciones no documentadas
- Ignorar los tests generados sin ejecutarlos
- Pedir refactorizaciones sin tests que protejan el comportamiento

#### ✅ Sí Hacer
- Seleccionar código antes de pedir cambios
- Referenciar archivos existentes como ejemplo
- Ejecutar tests después de cada cambio
- Usar las guías `.junie/guidelines/` como referencia

### 9. Integración con el Proyecto

#### Estructura de Módulos
```
backend/TrainFyre/src/main/java/es/codeurjc/students/trainfyre/
├── statistics/     # Dominio principal: incidencias
├── users/          # Usuarios y suscripciones
├── images/         # Upload de imágenes S3
└── security/       # JWT, OAuth2, autenticación
```

#### Al Pedir Ayuda, Especifica el Módulo
```
"En el módulo statistics, crea..."
"En el módulo users, implementa..."
```

### 10. Comandos de Verificación Rápidos

```bash
# Verificar tests unitarios
cd backend/TrainFyre && mvn test -Dtest='!*IntegrationTest,!*E2ETest'

# Verificar un test específico
cd backend/TrainFyre && mvn test -Dtest='IncidenceTest'

# Verificar arquitectura
cd backend/TrainFyre && mvn test -Dtest='*ArchitectureTest'

# Compilar sin tests
cd backend/TrainFyre && mvn compile
```

## 🎯 Regla de Oro

**Siempre da contexto específico:**
- Módulo del proyecto
- Archivo de referencia
- Patrón a seguir
- Herramientas a usar
- Casos a cubrir

**Cuanto más específico seas, mejor será el resultado de AI.**

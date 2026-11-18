---
name: tests
description: Write backend (Java) tests for a class or method from business rules the user gives, following TrainFyre's TDD red-phase conventions. Use when the user runs /tests or asks for tests of a backend class, method or requirement. Writes tests only, never production code.
argument-hint: <file or Class#method> <business rules / invariants>
disable-model-invocation: true
---

# /tests: red-phase tests for the backend

Input: a file or `Class#method` plus the business rules. The author already created the class/interface/signature and will implement it after committing the red tests. Backend only (`backend/TrainFyre/src/test`).

## Rules

1. **Tests only.** Never implement production logic. If the target does not compile yet, add only the minimum production stub needed (empty method body that throws `UnsupportedOperationException`) and say so. Do not touch it otherwise.
2. **Read first**: the target, its collaborators (ports, domain types) and one neighbouring test in the same package to match imports and setup. Reuse existing `*TestBuilder` (e.g. `statistics/IncidenceTestBuilder`); create `{Entity}TestBuilder` only for entities with 3+ params and none exists.
3. **Do not guess rules.** If an invariant, exception type or edge case is unclear, ask before writing. List the rules you will cover (one line each) so the author can correct them.
4. **One requirement = one test.** Group the cases of the same requirement into one `@ParameterizedTest` (`@MethodSource` returning `Stream<Arguments>` or `Stream<Executable>`, `@NullSource`, `@ValueSource`, `@CsvSource`). Use plain `@Test` only for a single-case requirement. Name each case for readable reports (`name = "..."`).
5. **Naming.** Class `{ClassName}Test`, package-private, in the package mirroring `src/main`. Methods `should[ExpectedBehavior]When[Condition]`. Data providers named after the scenario (`invalidNullArguments()`).
6. **Pick the type by layer** (path of the target decides):
   - `domain/`: pure JUnit 5 + AssertJ, no mocks, no Spring. Cover constructors, invariants (null, empty, bounds), state changes, value-object equality.
   - `application/service`: `@ExtendWith(MockitoExtension.class)`, mock output ports only, build the service in `@BeforeEach`, verify with `ArgumentCaptor`.
   - Commands/queries (`application/port/in`): validation tests as parameterized, like the domain.
   - Controllers: MockMvc standalone for unit; `*IntegrationTest` for Spring slices.
   - Persistence / adapters / E2E: names ending `IntegrationTest` or `E2ETest` (CI separates them by suffix), Testcontainers (PostgreSQL, and MariaDB where the E2E suite already does), Rest Assured for E2E.
7. **Style.** AssertJ `assertThat` / `assertThatThrownBy`; assert the specific exception type the rule demands (e.g. `NullPointerException` vs `IllegalArgumentException`: use what the user states, otherwise ask). Arrange-Act-Assert, independent and deterministic (no `now()` without a fixed clock). Assert behaviour, not implementation. No dead imports, no magic values repeated (extract constants), keep methods short. Tests count toward Sonar: no duplication, no unused code.
8. **Coverage of each rule**: happy path, edge cases (null, empty, limits), invariants, and state changes.

## Finish

Run only `./mvnw -q test-compile` (from `backend/TrainFyre`; `mvnw.cmd` on Windows) or, if it compiles, the single new test class with `-Dtest=<Class>` to confirm it fails for the intended reason (red). Do not try to make it green. Report: tests written (file, one line per requirement), anything assumed, and whether it compiled and why it fails. Do not commit. End the report by suggesting the commit messages the author can use, following the CLAUDE.md format `type(scope): short description`: one `test(...)` for the red tests and one `feat(...)` (or `fix`/`refactor`) for the implementation that will turn them green. Make them explicit: the description must name the business rules tested or implemented (e.g. `test(domain): TransportMap.addLine indexes lines, rejects duplicates and null, and sets lastUpdatedAt when an instant is given`), not just the method name. Both commits should mention the same rules.

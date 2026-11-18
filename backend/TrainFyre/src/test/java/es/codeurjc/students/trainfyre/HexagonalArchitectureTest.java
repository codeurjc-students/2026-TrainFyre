package es.codeurjc.students.trainfyre;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture rules applied to all production code by package convention, so new classes placed in
 * {@code domain}, {@code application} or {@code infrastructure} are checked without writing new tests.
 */
class HexagonalArchitectureTest {

    private static final String ROOT = "es.codeurjc.students.trainfyre";

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(ROOT);

    @Test
    void shouldRespectHexagonalLayers() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void shouldKeepDomainFreeOfFrameworks() {
        classes()
                .that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "org.jmolecules..", "lombok..", "..domain..",
                        "org.springframework.modulith")
                .because("the domain may only depend on the JDK, jMolecules, Lombok and Modulith's "
                        + "@NamedInterface, which exposes a package across modules")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void shouldNotAccessOutputAdaptersFromControllers() {
        noClasses()
                .that().resideInAPackage("..adapter.in.web..")
                .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
                .because("controllers must go through input ports and never touch persistence or storage")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void shouldNotAccessInfrastructureFromServices() {
        noClasses()
                .that().resideInAPackage("..application.service..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("services depend on output ports, not on adapters")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void shouldKeepPortsFreeOfInfrastructure() {
        noClasses()
                .that().resideInAnyPackage("..application.port..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("ports define the application boundary")
                .check(PRODUCTION_CLASSES);
    }

    @Test
    void shouldHaveNoCyclesBetweenModules() {
        slices().matching(ROOT + ".(*)..")
                .should().beFreeOfCycles()
                .check(PRODUCTION_CLASSES);
    }
}

package es.codeurjc.students.trainfyre;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.junit.jupiter.api.Test;

/**
 * Verifies every production class against the jMolecules annotations it declares (hexagonal ports and
 * adapters, DDD aggregates, entities and value objects), so new annotated classes are checked automatically.
 */
class JMoleculesArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.codeurjc.students.trainfyre");

    @Test
    void shouldRespectJMoleculesHexagonalArchitecture() {
        JMoleculesArchitectureRules.ensureHexagonal().check(PRODUCTION_CLASSES);
    }

    @Test
    void shouldRespectJMoleculesDddRules() {
        JMoleculesDddRules.all().check(PRODUCTION_CLASSES);
    }
}

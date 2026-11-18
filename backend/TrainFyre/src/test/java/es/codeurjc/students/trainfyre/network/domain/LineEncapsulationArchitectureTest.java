package es.codeurjc.students.trainfyre.network.domain;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LineEncapsulationArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.codeurjc.students.trainfyre");

    @Test
    void shouldOnlyAllowTransportMapToUpdateLines() {
        noClasses()
                .that().doNotHaveFullyQualifiedName(TransportMap.class.getName())
                .should().callMethod(Line.class, "update", LineChanges.class)
                .because("a line must only be modified through its aggregate root")
                .check(PRODUCTION_CLASSES);
    }
}

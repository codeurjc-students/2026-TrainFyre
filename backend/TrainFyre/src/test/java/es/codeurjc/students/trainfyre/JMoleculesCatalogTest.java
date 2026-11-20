package es.codeurjc.students.trainfyre;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.jmolecules.architecture.cqrs.Command;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.architecture.cqrs.QueryModel;
import org.jmolecules.architecture.hexagonal.Application;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jmolecules.ddd.annotation.Service;
import org.jmolecules.ddd.annotation.ValueObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generates, from the jMolecules annotations of all production classes, an AsciiDoc catalog per module,
 * a PlantUML domain class diagram per module and an {@code architecture.adoc} index that links every
 * generated artifact, including the Spring Modulith ones. Output goes to {@code target/architecture-docs}.
 */
class JMoleculesCatalogTest {

    private static final String ROOT = "es.codeurjc.students.trainfyre";
    // Not spring-modulith-docs: Modulith's Documenter empties that folder when it writes its own documents.
    private static final Path OUTPUT = Path.of("target", "architecture-docs");
    private static final String MODULITH_DOCS = "../spring-modulith-docs/";

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(ROOT);

    private static final List<Class<? extends Annotation>> DOMAIN_STEREOTYPES =
            List.of(AggregateRoot.class, Entity.class, ValueObject.class);

    private static final Map<String, Function<JavaClass, Stream<String>>> CATALOG = categories();

    @Test
    void shouldGenerateCatalogDomainDiagramsAndIndex() throws IOException {
        Map<String, List<JavaClass>> byModule = PRODUCTION_CLASSES.stream()
                .filter(JMoleculesCatalogTest::isTopLevelClass)
                .filter(javaClass -> moduleOf(javaClass) != null)
                .collect(Collectors.groupingBy(JMoleculesCatalogTest::moduleOf, TreeMap::new, Collectors.toList()));
        assertFalse(byModule.isEmpty(), "no modules were found under " + ROOT);

        Files.createDirectories(OUTPUT);
        StringBuilder catalog = new StringBuilder("= jMolecules catalog\n\n");
        Set<String> modulesWithDomainDiagram = new TreeSet<>();
        byModule.forEach((module, classes) -> {
            catalog.append(moduleCatalog(module, classes));
            String diagram = domainDiagram(module, classes);
            if (diagram != null) {
                writeString(OUTPUT.resolve("domain-" + module + ".puml"), diagram);
                modulesWithDomainDiagram.add(module);
            }
        });

        Path catalogFile = OUTPUT.resolve("jmolecules-catalog.adoc");
        writeString(catalogFile, catalog.toString());
        writeString(OUTPUT.resolve("architecture.adoc"), index(byModule.keySet(), modulesWithDomainDiagram));
        assertTrue(Files.size(catalogFile) > 0);
        assertFalse(modulesWithDomainDiagram.isEmpty(), "no domain classes annotated with jMolecules were found");
    }

    private static Map<String, Function<JavaClass, Stream<String>>> categories() {
        Map<String, Function<JavaClass, Stream<String>>> categories = new LinkedHashMap<>();
        categories.put("Aggregate roots", classNamed(c -> has(c, AggregateRoot.class)));
        categories.put("Entities", classNamed(c -> has(c, Entity.class)));
        categories.put("Value objects", classNamed(c -> has(c, ValueObject.class)));
        categories.put("Identities", JMoleculesCatalogTest::identities);
        categories.put("Domain services", classNamed(c -> has(c, Service.class)));
        categories.put("Commands", classNamed(c -> has(c, Command.class)));
        categories.put("Query models", classNamed(c -> has(c, QueryModel.class)));
        categories.put("Command handlers", JMoleculesCatalogTest::commandHandlers);
        categories.put("Primary ports", classNamed(c -> c.isInterface() && has(c, PrimaryPort.class)
                && !has(c, SecondaryPort.class)));
        categories.put("Primary adapters", classNamed(c -> !c.isInterface() && has(c, PrimaryAdapter.class)));
        categories.put("Application services", classNamed(c -> !c.isInterface() && has(c, Application.class)));
        categories.put("Secondary ports", classNamed(c -> c.isInterface() && has(c, SecondaryPort.class)));
        categories.put("Secondary adapters", classNamed(c -> !c.isInterface() && has(c, SecondaryAdapter.class)));
        return categories;
    }

    private static Function<JavaClass, Stream<String>> classNamed(java.util.function.Predicate<JavaClass> filter) {
        return javaClass -> filter.test(javaClass) ? Stream.of(javaClass.getSimpleName()) : Stream.empty();
    }

    private static Stream<String> identities(JavaClass javaClass) {
        return javaClass.getFields().stream()
                .filter(field -> field.isAnnotatedWith(Identity.class))
                .map(field -> javaClass.getSimpleName() + "." + field.getName() + " : " + field.getRawType().getSimpleName());
    }

    private static Stream<String> commandHandlers(JavaClass javaClass) {
        return javaClass.getMethods().stream()
                .filter(method -> method.isAnnotatedWith(CommandHandler.class))
                .map(method -> javaClass.getSimpleName() + "#" + method.getName());
    }

    private static boolean has(JavaClass javaClass, Class<? extends Annotation> annotation) {
        return javaClass.isAnnotatedWith(annotation) || packageHas(javaClass.getPackage(), annotation);
    }

    private static boolean packageHas(JavaPackage javaPackage, Class<? extends Annotation> annotation) {
        return javaPackage.isAnnotatedWith(annotation)
                || javaPackage.getParent().map(parent -> packageHas(parent, annotation)).orElse(false);
    }

    private static boolean isTopLevelClass(JavaClass javaClass) {
        return javaClass.getEnclosingClass().isEmpty() && !javaClass.getSimpleName().equals("package-info");
    }

    private static String moduleOf(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        if (!packageName.startsWith(ROOT + ".")) {
            return null;
        }
        String relative = packageName.substring(ROOT.length() + 1);
        int dot = relative.indexOf('.');
        return dot < 0 ? relative : relative.substring(0, dot);
    }

    private static String moduleCatalog(String module, List<JavaClass> classes) {
        StringBuilder rows = new StringBuilder();
        CATALOG.forEach((title, extractor) -> {
            List<String> entries = classes.stream().flatMap(extractor).distinct().sorted().toList();
            if (!entries.isEmpty()) {
                rows.append('|').append(title).append('\n')
                        .append('|').append(entries.stream().map(entry -> "* `" + entry + "`")
                                .collect(Collectors.joining("\n"))).append('\n');
            }
        });
        if (rows.isEmpty()) {
            return "";
        }
        return "== " + module + "\n\n[%autowidth.stretch, cols=\"h,a\"]\n|===\n" + rows + "|===\n\n";
    }

    private static String domainDiagram(String module, List<JavaClass> classes) {
        List<JavaClass> domain = classes.stream()
                .filter(javaClass -> DOMAIN_STEREOTYPES.stream().anyMatch(javaClass::isAnnotatedWith))
                .sorted((a, b) -> a.getSimpleName().compareTo(b.getSimpleName()))
                .toList();
        if (domain.isEmpty()) {
            return null;
        }
        StringBuilder puml = new StringBuilder("@startuml\ntitle Domain model: ").append(module)
                .append("\nhide empty members\n\n");
        domain.forEach(javaClass -> puml.append(classDeclaration(javaClass)));
        puml.append('\n');
        domain.stream().flatMap(javaClass -> relations(javaClass, domain))
                .collect(Collectors.toCollection(TreeSet::new))
                .forEach(line -> puml.append(line).append('\n'));
        return puml.append("@enduml\n").toString();
    }

    private static String classDeclaration(JavaClass javaClass) {
        String stereotype = DOMAIN_STEREOTYPES.stream().filter(javaClass::isAnnotatedWith)
                .map(Class::getSimpleName).findFirst().orElse("");
        String kind = javaClass.isEnum() ? "enum " : javaClass.isInterface() ? "interface " : "class ";
        StringBuilder body = new StringBuilder();
        javaClass.getFields().stream().filter(field -> field.isAnnotatedWith(Identity.class))
                .forEach(field -> body.append("  +").append(field.getName()).append(" : ")
                        .append(field.getRawType().getSimpleName()).append("  [identity]\n"));
        return kind + javaClass.getSimpleName() + " <<" + stereotype + ">> {\n" + body + "}\n";
    }

    private static Stream<String> relations(JavaClass origin, List<JavaClass> domain) {
        return origin.getFields().stream()
                .filter(field -> !field.getModifiers().contains(JavaModifier.STATIC))
                .flatMap(field -> field.getType().getAllInvolvedRawTypes().stream()
                        .filter(domain::contains)
                        .filter(target -> !target.equals(origin))
                        .map(target -> relation(origin, target, field)));
    }

    private static String relation(JavaClass origin, JavaClass target, JavaField field) {
        return origin.getSimpleName() + " --> " + target.getSimpleName() + " : " + field.getName();
    }

    private static String index(Set<String> modules, Set<String> modulesWithDomainDiagram) {
        StringBuilder adoc = new StringBuilder("= Architecture\n\n== Module dependencies\n\n")
                .append("plantuml::modules-dependencies.puml[]\n\n== Spring Modulith components\n\n")
                .append("plantuml::").append(MODULITH_DOCS).append("components.puml[]\n\n")
                .append("include::").append(MODULITH_DOCS).append("all-docs.adoc[leveloffset=+1]\n\n");
        modules.forEach(module -> {
            adoc.append("== Hexagonal view: ").append(module).append("\n\n")
                    .append("plantuml::hexagonal-").append(module).append(".puml[]\n\n");
            if (modulesWithDomainDiagram.contains(module)) {
                adoc.append("=== Domain model\n\nplantuml::domain-").append(module).append(".puml[]\n\n");
            }
        });
        return adoc.append("include::jmolecules-catalog.adoc[leveloffset=+1]\n").toString();
    }

    private static void writeString(Path path, String content) {
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write " + path, e);
        }
    }
}

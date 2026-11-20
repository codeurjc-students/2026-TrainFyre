package es.codeurjc.students.trainfyre;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generates one PlantUML diagram per module showing its hexagonal components and their real dependencies,
 * plus a module-level overview. Output goes to {@code target/architecture-docs}.
 */
class ArchitectureDiagramTest {

    private static final String ROOT = "es.codeurjc.students.trainfyre";
    // Not spring-modulith-docs: Modulith's Documenter empties that folder when it writes its own documents.
    private static final Path OUTPUT = Path.of("target", "architecture-docs");

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(ROOT);

    private static final Comparator<Node> NODE_ORDER =
            Comparator.comparing(Node::module).thenComparing(Node::component);

    private record Node(String module, String component) {
        String alias() {
            return (module + "_" + component).replace('.', '_');
        }

        String label() {
            return module + "." + component;
        }
    }

    private record Edge(Node from, Node to) {
    }

    @Test
    void shouldGenerateHexagonalDiagramPerModule() throws IOException {
        Set<Node> nodes = new TreeSet<>(NODE_ORDER);
        Set<Edge> edges = new TreeSet<>(Comparator.comparing(Edge::from, NODE_ORDER).thenComparing(Edge::to, NODE_ORDER));
        collect(nodes, edges);

        Set<String> modules = nodes.stream().map(Node::module).collect(Collectors.toCollection(TreeSet::new));
        assertFalse(modules.isEmpty(), "no modules were found under " + ROOT);

        Files.createDirectories(OUTPUT);
        for (String module : modules) {
            Path file = OUTPUT.resolve("hexagonal-" + module + ".puml");
            Files.writeString(file, moduleDiagram(module, nodes, edges));
            assertTrue(Files.size(file) > 0, "empty diagram " + file);
        }
        Path overview = OUTPUT.resolve("modules-dependencies.puml");
        Files.writeString(overview, overviewDiagram(modules, edges));
        assertTrue(Files.size(overview) > 0);
    }

    private static void collect(Set<Node> nodes, Set<Edge> edges) {
        for (JavaClass javaClass : PRODUCTION_CLASSES) {
            Optional<Node> origin = nodeOf(javaClass);
            if (origin.isEmpty()) {
                continue;
            }
            nodes.add(origin.get());
            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                nodeOf(dependency.getTargetClass())
                        .filter(target -> !target.equals(origin.get()))
                        .ifPresent(target -> {
                            nodes.add(target);
                            edges.add(new Edge(origin.get(), target));
                        });
            }
        }
    }

    private static Optional<Node> nodeOf(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        if (!packageName.startsWith(ROOT + ".")) {
            return Optional.empty();
        }
        String relative = packageName.substring(ROOT.length() + 1);
        int dot = relative.indexOf('.');
        String module = dot < 0 ? relative : relative.substring(0, dot);
        String inner = dot < 0 ? "" : relative.substring(dot + 1);
        return Optional.of(new Node(module, componentOf(inner)));
    }

    private static String componentOf(String inner) {
        if (inner.startsWith("domain")) {
            return "domain";
        }
        if (inner.startsWith("application.port.in")) {
            return "port.in";
        }
        if (inner.startsWith("application.port.out")) {
            return "port.out";
        }
        if (inner.startsWith("application.service")) {
            return "service";
        }
        if (inner.startsWith("infrastructure.adapter.in")) {
            return "adapter.in";
        }
        if (inner.startsWith("infrastructure.adapter.out")) {
            return "adapter.out";
        }
        return inner.startsWith("infrastructure") ? "infrastructure" : "other";
    }

    private static String moduleDiagram(String module, Set<Node> nodes, Set<Edge> edges) {
        Set<Edge> moduleEdges = edges.stream()
                .filter(edge -> edge.from().module().equals(module))
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(Edge::from, NODE_ORDER).thenComparing(Edge::to, NODE_ORDER))));

        StringBuilder puml = new StringBuilder("@startuml\ntitle Hexagonal view: ").append(module)
                .append("\nleft to right direction\n\npackage \"").append(module).append("\" {\n");
        nodes.stream().filter(node -> node.module().equals(module)).forEach(node ->
                puml.append("  component \"").append(node.component()).append("\" as ").append(node.alias()).append('\n'));
        puml.append("}\n\n");

        moduleEdges.stream().map(Edge::to).filter(node -> !node.module().equals(module))
                .collect(Collectors.toCollection(() -> new TreeSet<>(NODE_ORDER)))
                .forEach(node -> puml.append("component \"").append(node.label()).append("\" as ")
                        .append(node.alias()).append(" #LightGray\n"));
        puml.append('\n');

        moduleEdges.forEach(edge -> puml.append(edge.from().alias()).append(" --> ")
                .append(edge.to().alias()).append('\n'));
        return puml.append("@enduml\n").toString();
    }

    private static String overviewDiagram(Set<String> modules, Set<Edge> edges) {
        StringBuilder puml = new StringBuilder("@startuml\ntitle Module dependencies\n\n");
        modules.forEach(module -> puml.append("component \"").append(module).append("\" as ").append(module).append('\n'));
        puml.append('\n');
        edges.stream()
                .filter(edge -> !edge.from().module().equals(edge.to().module()))
                .map(edge -> edge.from().module() + " --> " + edge.to().module())
                .collect(Collectors.toCollection(TreeSet::new))
                .forEach(line -> puml.append(line).append('\n'));
        return puml.append("@enduml\n").toString();
    }
}

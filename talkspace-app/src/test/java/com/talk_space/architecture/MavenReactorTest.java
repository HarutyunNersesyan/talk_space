package com.talk_space.architecture;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Build boundaries complement the Java package boundaries. */
class MavenReactorTest {
    private static final Path ROOT = Path.of(System.getProperty("talkspace.projectRoot"));
    private static final Set<String> MODULES = Set.of("talkspace-shared", "talkspace-users", "talkspace-auth",
            "talkspace-notifications", "talkspace-network", "talkspace-discovery", "talkspace-chat",
            "talkspace-feedback", "talkspace-presence", "talkspace-app");

    @Test void parentAggregatesSeparateSourceTrees() throws Exception {
        Document parent = pom(ROOT);
        assertEquals("pom", parent.getElementsByTagName("packaging").item(0).getTextContent());
        assertEquals(MODULES, Set.copyOf(values(parent, "module")));
        assertFalse(Files.exists(ROOT.resolve("src")), "The aggregator must not own application sources");
        for (String module : MODULES) {
            Path directory = ROOT.resolve(module);
            assertTrue(Files.isRegularFile(directory.resolve("pom.xml")), module);
            try (var sources = Files.walk(directory.resolve("src/main/java"))) {
                assertTrue(sources.anyMatch(p -> p.toString().endsWith(".java")), module);
            }
            var childParent = pom(directory).getElementsByTagName("parent").item(0);
            assertTrue(childParent.getTextContent().contains("talkspace-parent"), module);
        }
    }

    @Test void onlyAppDeclaresExecutableBootPackaging() throws Exception {
        for (String module : MODULES) {
            var plugins = pom(ROOT.resolve(module)).getElementsByTagName("plugin");
            boolean bootPlugin = false;
            for (int i = 0; i < plugins.getLength(); i++) {
                if (plugins.item(i).getTextContent().contains("spring-boot-maven-plugin")) bootPlugin = true;
            }
            assertEquals(module.equals("talkspace-app"), bootPlugin, module);
        }
    }

    @Test void architectureScanIncludesEveryFeatureJar() {
        var classes = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.talk_space");
        for (String feature : List.of("users", "auth", "notifications", "network", "discovery", "chat", "feedback", "presence")) {
            assertTrue(classes.stream().anyMatch(c -> c.getPackageName().startsWith("com.talk_space.modules." + feature + ".")), feature);
        }
    }

    private static Document pom(Path directory) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(directory.resolve("pom.xml").toFile());
    }
    private static List<String> values(Document document, String tag) {
        List<String> values = new ArrayList<>();
        var nodes = document.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) values.add(nodes.item(i).getTextContent());
        return values;
    }
}

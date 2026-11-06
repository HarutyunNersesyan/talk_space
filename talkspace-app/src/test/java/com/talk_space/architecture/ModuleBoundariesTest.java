package com.talk_space.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleBoundariesTest {
    private static final String ROOT = "com.talk_space.modules.";
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.talk_space");
    private static final Map<String, Set<String>> DEPENDENCIES = Map.of(
            "users", Set.of(), "auth", Set.of("users"),
            "notifications", Set.of("users"), "network", Set.of("users", "notifications"),
            "discovery", Set.of("users", "network"), "chat", Set.of("users", "network"),
            "feedback", Set.of("users"), "presence", Set.of("users", "network"));

    @Test void featureModulesHaveNoCycles() {
        slices().matching(ROOT + "(*)..").should().beFreeOfCycles().check(CLASSES);
    }

    @Test void otherModulesUseOnlyPublicApis() {
        for (String module : DEPENDENCIES.keySet()) {
            noClasses().that().resideInAPackage(ROOT + ".")
                    .and().resideOutsideOfPackage(ROOT + module + "..")
                    .should().dependOnClassesThat().resideInAPackage(ROOT + module + ".internal..")
                    .check(CLASSES);
        }
    }

    @Test void dependenciesFollowTheDeclaredGraph() {
        for (var source : CLASSES) {
            if (!source.getPackageName().startsWith(ROOT)) continue;
            String from = source.getPackageName().substring(ROOT.length()).split("\\.")[0];
            assertTrue(DEPENDENCIES.containsKey(from), "Unregistered module: " + from);
            for (var dependency : source.getDirectDependenciesFromSelf()) {
                String target = dependency.getTargetClass().getPackageName();
                if (!target.startsWith(ROOT)) continue;
                String to = target.substring(ROOT.length()).split("\\.")[0];
                assertTrue(from.equals(to) || DEPENDENCIES.get(from).contains(to), dependency.getDescription());
            }
        }
    }

    @Test void modulesDoNotDependOnBootstrap() {
        noClasses().that().resideInAPackage(ROOT + ".")
                .should().dependOnClassesThat().resideInAPackage("com.talk_space.bootstrap..")
                .check(CLASSES);
    }

    @Test void sharedCodeDoesNotDependOnFeaturesOrBootstrap() {
        noClasses().that().resideInAPackage("com.talk_space.shared..")
                .should().dependOnClassesThat().resideInAnyPackage(ROOT + ".", "com.talk_space.bootstrap..")
                .check(CLASSES);
    }

    @Test void allProductionCodeHasAnOwner() {
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .should().resideInAnyPackage("com.talk_space", ROOT + ".",
                        "com.talk_space.bootstrap..", "com.talk_space.shared..")
                .check(CLASSES);
    }
}

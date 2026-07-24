package com.bankingsystem.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.bankingsystem",
        importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

    @ArchTest
    static final ArchRule domain_is_framework_and_outer_layer_independent =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..application..",
                            "..infrastructure..",
                            "..presentation..",
                            "..bootstrap..",
                            "org.springframework..",
                            "jakarta..")
                    .because("domain rules must remain framework-independent");

    @ArchTest
    static final ArchRule application_does_not_depend_on_adapters =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..infrastructure..",
                            "..presentation..",
                            "..bootstrap..",
                            "org.springframework..",
                            "jakarta..")
                    .because("application use cases must depend only on domain types and ports");

    @ArchTest
    static final ArchRule presentation_does_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
                    .because("HTTP adapters must call application input ports");

    @ArchTest
    static final ArchRule infrastructure_does_not_depend_on_presentation =
            noClasses()
                    .that().resideInAPackage("..infrastructure..")
                    .should().dependOnClassesThat().resideInAnyPackage("..presentation..")
                    .because("output adapters must not depend on HTTP concerns");

    @ArchTest
    static final ArchRule controllers_live_in_presentation_packages =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..presentation..")
                    .because("controllers are inbound presentation adapters");
}

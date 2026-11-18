package com.eventhub.boleteria;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.eventhub.boleteria")

public class ArquitecturaTest {
    @ArchTest
    static final ArchRule domainNoDependeDeNadie = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "..interfaces..",
                    "org.springframework..", "jakarta..", "org.hibernate..");

    @ArchTest
    static final ArchRule applicationNoDependeDeInfNiInt = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..interfaces..");

    @ArchTest
    static final ArchRule interfacesNoDependeDeInf = noClasses().that().resideInAPackage("..interfaces..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");
}


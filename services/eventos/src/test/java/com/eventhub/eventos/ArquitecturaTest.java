package com.eventhub.eventos;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.eventhub.eventos")

// TODO(#19): quitar allowEmptyShould cuando domain/application tengan clases
public class ArquitecturaTest {
    @ArchTest
    static final ArchRule domainNoDependeDeNadie = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "..interfaces..", "org.springframework..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationNoDependeDeInfNiInt = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..interfaces..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule interfacesNoDependeDeInt = noClasses().that().resideInAPackage("..interfaces..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
            .allowEmptyShould(true);
}

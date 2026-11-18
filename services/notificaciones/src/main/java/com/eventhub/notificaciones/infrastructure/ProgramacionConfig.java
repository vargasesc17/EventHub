package com.eventhub.notificaciones.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Activa las tareas programadas (RecordatoriosJob). En los tests no corren. */
@Configuration
@EnableScheduling
@Profile("!test")
public class ProgramacionConfig {
}

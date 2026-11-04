package com.biblioteca.config;

import com.biblioteca.service.PrestamoService;
import com.biblioteca.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Procesos automáticos del actor "Sistema" (CU-12, RN-08 y RN-09). */
@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final PrestamoService prestamoService;
    private final ReservaService reservaService;

    public TareasProgramadas(PrestamoService prestamoService, ReservaService reservaService) {
        this.prestamoService = prestamoService;
        this.reservaService = reservaService;
    }

    /** Se ejecuta al iniciar la aplicación y todos los días a las 00:05. */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")
    public void ejecutar() {
        int vencidos = prestamoService.actualizarVencidos();
        int expiradas = reservaService.procesarExpiradas();
        log.info("Tareas diarias: {} préstamos marcados como vencidos, {} reservas expiradas", vencidos, expiradas);
    }
}

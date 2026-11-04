package com.biblioteca.controller;

import com.biblioteca.dto.DashboardResponse;
import com.biblioteca.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** RF-16: indicadores del panel principal. */
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public DashboardResponse resumen() {
        return dashboardService.resumen();
    }
}

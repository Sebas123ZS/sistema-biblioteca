package com.biblioteca.dto;

import java.util.List;

public record DashboardResponse(
        long totalTitulos,
        long totalEjemplares,
        long ejemplaresDisponibles,
        long usuariosRegistrados,
        long usuariosNuevosMes,
        long prestamosActivos,
        long prestamosVencidos,
        long devolucionesHoy,
        long devolucionesMes,
        long reservasPendientes,
        long reservasDisponibles,
        List<CategoriaConteo> categorias,
        List<LibroResponse> ultimosLibros
) {
    public record CategoriaConteo(String nombre, long total) {
    }
}

package com.biblioteca.dto;

public record DisponibilidadResponse(
        Long libroId,
        String titulo,
        int cantidadTotal,
        int cantidadDisponible,
        int cantidadPrestada,
        boolean disponible,
        long reservasEnEspera
) {
}

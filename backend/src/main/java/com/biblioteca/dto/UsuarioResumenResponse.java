package com.biblioteca.dto;

/** Totales que se muestran en el perfil del usuario. */
public record UsuarioResumenResponse(
        UsuarioResponse usuario,
        long prestamosActivos,
        long prestamosVencidos,
        long prestamosDevueltos,
        long reservasActivas
) {
}

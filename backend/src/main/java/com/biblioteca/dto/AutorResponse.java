package com.biblioteca.dto;

import com.biblioteca.model.Autor;

public record AutorResponse(
        Long id,
        String codigo,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String nacionalidad,
        String biografia,
        long cantidadLibros
) {
    public static AutorResponse from(Autor a, long cantidadLibros) {
        return new AutorResponse(a.getId(), String.format("AUT-%03d", a.getId()), a.getNombres(),
                a.getApellidos(), a.getNombreCompleto(), a.getNacionalidad(), a.getBiografia(), cantidadLibros);
    }
}

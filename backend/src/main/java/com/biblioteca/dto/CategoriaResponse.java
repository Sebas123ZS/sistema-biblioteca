package com.biblioteca.dto;

import com.biblioteca.model.Categoria;

/** Categoría con la cantidad de títulos activos que tiene (RF-17). */
public record CategoriaResponse(Long id, String nombre, String descripcion, long cantidadLibros) {

    public static CategoriaResponse from(Categoria c, long cantidadLibros) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), cantidadLibros);
    }
}

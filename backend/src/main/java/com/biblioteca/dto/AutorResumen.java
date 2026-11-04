package com.biblioteca.dto;

import com.biblioteca.model.Autor;

public record AutorResumen(Long id, String nombreCompleto) {

    public static AutorResumen from(Autor a) {
        return new AutorResumen(a.getId(), a.getNombreCompleto());
    }
}

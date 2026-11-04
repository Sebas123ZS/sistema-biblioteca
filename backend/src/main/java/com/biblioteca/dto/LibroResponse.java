package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Libro;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record LibroResponse(
        Long id,
        String codigo,
        String isbn,
        String titulo,
        String editorial,
        Integer anioPublicacion,
        Long categoriaId,
        String categoriaNombre,
        List<AutorResumen> autores,
        Integer cantidadTotal,
        Integer cantidadDisponible,
        boolean disponible,
        String portadaUrl,
        LocalDateTime fechaRegistro
) {
    public static LibroResponse from(Libro l) {
        List<AutorResumen> autores = l.getAutores().stream()
                .sorted(Comparator.comparing(Autor::getApellidos))
                .map(AutorResumen::from)
                .toList();
        return new LibroResponse(l.getId(), String.format("LIB-%03d", l.getId()), l.getIsbn(), l.getTitulo(),
                l.getEditorial(), l.getAnioPublicacion(), l.getCategoria().getId(), l.getCategoria().getNombre(),
                autores, l.getCantidadTotal(), l.getCantidadDisponible(), l.estaDisponible(), l.getPortadaUrl(),
                l.getFechaRegistro());
    }
}

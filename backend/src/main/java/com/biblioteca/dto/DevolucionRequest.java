package com.biblioteca.dto;

import com.biblioteca.model.enums.EstadoLibroDevuelto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DevolucionRequest(
        @NotNull(message = "Indique el préstamo a cerrar") Long prestamoId,
        EstadoLibroDevuelto estadoLibro,
        @Size(max = 500, message = "Máximo 500 caracteres") String observaciones
) {
}

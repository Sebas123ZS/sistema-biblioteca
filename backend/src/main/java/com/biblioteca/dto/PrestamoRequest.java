package com.biblioteca.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Si no se envía fechaVencimiento se usa el plazo por defecto (14 días). */
public record PrestamoRequest(
        @NotNull(message = "Seleccione un usuario") Long usuarioId,
        @NotNull(message = "Seleccione un libro") Long libroId,
        @FutureOrPresent(message = "La fecha de vencimiento no puede ser pasada") LocalDate fechaVencimiento
) {
}

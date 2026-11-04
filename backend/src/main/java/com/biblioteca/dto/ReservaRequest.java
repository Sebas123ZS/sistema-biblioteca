package com.biblioteca.dto;

import jakarta.validation.constraints.NotNull;

/**
 * usuarioId solo lo usa el bibliotecario (reserva a nombre de otro usuario).
 * Si quien reserva es un LECTOR, siempre se usa su propio id.
 */
public record ReservaRequest(
        @NotNull(message = "Seleccione un libro") Long libroId,
        Long usuarioId
) {
}

package com.biblioteca.dto;

import com.biblioteca.model.enums.EstadoUsuario;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoUsuarioRequest(
        @NotNull(message = "El estado es obligatorio") EstadoUsuario estado
) {
}

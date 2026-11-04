package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutorRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 80, message = "Máximo 80 caracteres") String nombres,
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Máximo 80 caracteres") String apellidos,
        @Size(max = 60, message = "Máximo 60 caracteres") String nacionalidad,
        @Size(max = 1000, message = "Máximo 1000 caracteres") String biografia
) {
}

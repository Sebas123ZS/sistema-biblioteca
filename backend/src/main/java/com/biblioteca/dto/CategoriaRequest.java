package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "Máximo 60 caracteres") String nombre,
        @Size(max = 255, message = "Máximo 255 caracteres") String descripcion
) {
}

package com.biblioteca.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public record LibroRequest(
        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^[0-9Xx-]{10,17}$", message = "ISBN inválido (10 o 13 dígitos, puede incluir guiones)")
        String isbn,
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "Máximo 200 caracteres") String titulo,
        @Size(max = 120, message = "Máximo 120 caracteres") String editorial,
        @Min(value = 1000, message = "Año inválido")
        @Max(value = 2100, message = "Año inválido") Integer anioPublicacion,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId,
        @NotEmpty(message = "Seleccione al menos un autor") Set<Long> autorIds,
        @NotNull(message = "La cantidad de ejemplares es obligatoria")
        @Min(value = 1, message = "Debe haber al menos 1 ejemplar")
        @Max(value = 500, message = "Máximo 500 ejemplares") Integer cantidadTotal,
        @Size(max = 500, message = "Máximo 500 caracteres") String portadaUrl
) {
}

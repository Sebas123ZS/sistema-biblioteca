package com.biblioteca.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AsignarAutoresRequest(
        @NotEmpty(message = "Seleccione al menos un autor") Set<Long> autorIds
) {
}

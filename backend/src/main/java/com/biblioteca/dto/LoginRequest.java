package com.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingrese su usuario o correo") String login,
        @NotBlank(message = "Ingrese su contraseña") String password
) {
}

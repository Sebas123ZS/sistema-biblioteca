package com.biblioteca.dto;

import jakarta.validation.constraints.*;

/** Autorregistro público de lectores (RF-19). El rol siempre es LECTOR. */
public record RegistroRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 80, message = "Máximo 80 caracteres") String nombres,
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Máximo 80 caracteres") String apellidos,
        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = "^[0-9]{8,12}$", message = "El documento debe tener de 8 a 12 dígitos") String documento,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        @Size(max = 120, message = "Máximo 120 caracteres") String correo,
        @Pattern(regexp = "^$|^[0-9 +]{6,15}$", message = "Teléfono inválido") String telefono,
        @Size(max = 200, message = "Máximo 200 caracteres") String direccion,
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{4,50}$", message = "Usuario: 4 a 50 caracteres (letras, números, . _ -)") String username,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 64, message = "La contraseña debe tener entre 6 y 64 caracteres") String password
) {
}

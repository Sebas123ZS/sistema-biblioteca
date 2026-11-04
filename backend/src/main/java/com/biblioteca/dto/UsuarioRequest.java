package com.biblioteca.dto;

import com.biblioteca.model.enums.Rol;
import jakarta.validation.constraints.*;

/**
 * Para crear y actualizar usuarios. La contraseña es obligatoria al crear
 * y opcional al actualizar (si viene vacía se conserva la actual).
 */
public record UsuarioRequest(
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
        @Size(max = 64, message = "Máximo 64 caracteres") String password,
        @NotNull(message = "El rol es obligatorio") Rol rol
) {
}

package com.biblioteca.dto;

import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.model.enums.Rol;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String codigo,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String documento,
        String correo,
        String telefono,
        String direccion,
        String username,
        Rol rol,
        EstadoUsuario estado,
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), String.format("U-%03d", u.getId()), u.getNombres(), u.getApellidos(),
                u.getNombreCompleto(), u.getDocumento(), u.getCorreo(), u.getTelefono(), u.getDireccion(),
                u.getUsername(), u.getRol(), u.getEstado(), u.getFechaRegistro());
    }
}

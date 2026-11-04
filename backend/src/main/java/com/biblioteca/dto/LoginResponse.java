package com.biblioteca.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEn,
        UsuarioResponse usuario
) {
}

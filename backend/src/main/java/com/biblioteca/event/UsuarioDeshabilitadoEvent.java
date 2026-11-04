package com.biblioteca.event;

/**
 * Evento que se publica cuando un usuario pasa de ACTIVO a SUSPENDIDO o INACTIVO (RN-13).
 * Lo atiende ReservaService dentro de la misma transacción para cancelar sus reservas.
 * Se usa un evento para no crear una dependencia circular entre UsuarioService y ReservaService.
 */
public record UsuarioDeshabilitadoEvent(Long usuarioId) {
}

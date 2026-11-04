package com.biblioteca.model.enums;

/**
 * PENDIENTE  : en lista de espera.
 * DISPONIBLE : se le apartó una unidad devuelta; tiene N días para retirarla.
 * ATENDIDA   : el usuario retiró el libro (se generó el préstamo).
 * CANCELADA  : anulada por el usuario o el bibliotecario.
 * EXPIRADA   : no se retiró dentro del plazo.
 */
public enum EstadoReserva {
    PENDIENTE,
    DISPONIBLE,
    ATENDIDA,
    CANCELADA,
    EXPIRADA
}

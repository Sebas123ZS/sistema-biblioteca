package com.biblioteca.exception;

/** Se lanza cuando una operación viola una regla de negocio (HTTP 409). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

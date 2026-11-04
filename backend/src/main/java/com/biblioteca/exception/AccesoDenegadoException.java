package com.biblioteca.exception;

/** Se lanza cuando un LECTOR intenta ver o modificar datos de otro usuario (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}

package com.salas.reuniones.common;

/** Se lanza cuando la operación choca con el estado actual (p. ej. horario ocupado). Se traduce a HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

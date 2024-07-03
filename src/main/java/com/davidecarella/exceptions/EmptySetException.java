package com.davidecarella.exceptions;

/**
 * Eccezione lanciata quando si prova a caricare dei dati da una tabella vuota.
 */
public class EmptySetException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code} message, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public EmptySetException(String message) {
        super(message);
    }
}

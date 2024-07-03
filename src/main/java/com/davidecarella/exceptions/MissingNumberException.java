package com.davidecarella.exceptions;

/**
 * Eccezione lanciata quando si prova a caricare delle transazioni da una tabella che contiene degli attributi non
 * numerici.
 */
public class MissingNumberException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code} message, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public MissingNumberException(String message) {
        super(message);
    }
}

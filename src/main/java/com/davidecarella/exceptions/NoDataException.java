package com.davidecarella.exceptions;

/**
 * Eccezione lanciata quando si incontrano degli errori durante il caricamento degli esempi da una tabella.
 */
public class NoDataException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public NoDataException(String message) {
        super(message);
    }

    /**
     * Costruisce l'eccezione con messaggio {@code message} e causa {@code cause}, entrambi specificati come parametro.
     *
     * @param message il messaggio dell'eccezione
     * @param cause la causa dell'eccezione
     */
    public NoDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.davidecarella.exceptions;

/**
 * Eccezione lanciata quando si incontra un errore durante la connessione al database.
 */
public class DatabaseConnectionException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * Costruisce l'eccezione con messaggio {@code message} e causa {@code cause}, entrambi specificati come parametro.
     *
     * @param message il messaggio dell'eccezione
     * @param cause la causa dell'eccezione
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

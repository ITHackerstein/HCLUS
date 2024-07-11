package com.davidecarella.hclus.server.exceptions;

/**
 * Eccezione lanciata quando si prova a caricare un clustering con degli indici di cluster sbagliati.
 */
public class InvalidClusterIndexException extends Exception {
    /**
     * Costruisce l'eccezione con il messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public InvalidClusterIndexException(String message) {
        super(message);
    }
}

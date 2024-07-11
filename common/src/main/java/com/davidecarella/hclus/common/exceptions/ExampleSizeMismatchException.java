package com.davidecarella.hclus.common.exceptions;

/**
 * Eccezione lanciata quando si tenta di calcolare la distanza tra esempi di dimensione diversa.
 */
public class ExampleSizeMismatchException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public ExampleSizeMismatchException(String message) {
        super(message);
    }
}

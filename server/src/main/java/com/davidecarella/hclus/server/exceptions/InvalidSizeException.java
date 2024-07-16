package com.davidecarella.hclus.server.exceptions;

import com.davidecarella.hclus.server.data.Example;

/**
 * Eccezione lanciata quando si prova a calcolare la distanza fra due {@link Example esempi} con lunghezze diverse.
 */
public class InvalidSizeException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public InvalidSizeException(String message) {
        super(message);
    }
}
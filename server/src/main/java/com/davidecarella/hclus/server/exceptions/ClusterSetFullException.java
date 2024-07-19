package com.davidecarella.hclus.server.exceptions;


/**
 * Eccezione lanciata quando si raggiunge la capienza massima di un cluster set.
 */
public class ClusterSetFullException extends RuntimeException {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public ClusterSetFullException(String message) {
        super(message);
    }
}

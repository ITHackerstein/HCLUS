package com.davidecarella.hclus.server.exceptions;

/**
 * Eccezione lanciata quando si prova ad unire i due cluster più vicini in un cluster set che contiene un solo cluster.
 */
public class ClusterSetTooSmallException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public ClusterSetTooSmallException(String message) {
        super(message);
    }
}

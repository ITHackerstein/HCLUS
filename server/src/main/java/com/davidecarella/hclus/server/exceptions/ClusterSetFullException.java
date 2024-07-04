package com.davidecarella.hclus.server.exceptions;

import com.davidecarella.hclus.server.clustering.ClusterSet;

/**
 * Eccezione lanciata quando si raggiunge la capienza massima di un {@link ClusterSet}
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

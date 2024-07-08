package com.davidecarella.hclus.server.exceptions;

import com.davidecarella.hclus.server.clustering.HierarchicalClustering;

/**
 * Eccezione lanciata quando si utilizza una profondità errata per un {@link HierarchicalClustering miner}.
 */
public class InvalidDepthException extends Exception {
    /**
     * Costruisce l'eccezione con messaggio {@code message}, specificato come parametro.
     *
     * @param message il messaggio dell'eccezione
     */
    public InvalidDepthException(String message) {
        super(message);
    }
}

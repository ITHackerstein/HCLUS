package com.davidecarella.hclus.server.exceptions;

import com.davidecarella.hclus.server.data.Data;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.clustering.ClusterSet;
import com.davidecarella.hclus.server.clustering.Cluster;

/**
 * Eccezione lanciata quando si prova a eseguire {@link ClusterSet#mergeClosestClusters(ClusterDistance, Data)}
 * su un {@link ClusterSet} con meno di due {@link Cluster cluster}.
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

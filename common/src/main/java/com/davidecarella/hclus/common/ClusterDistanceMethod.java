package com.davidecarella.hclus.common;

/**
 * Un metodo per calcolare la distanza fra cluster.
 *
 * @param id l'identificatore numerico del metodo
 * @param displayName il nome del metodo
 */
public record ClusterDistanceMethod(int id, String displayName) {
    @Override
    public String toString() {
        return this.displayName;
    }
}
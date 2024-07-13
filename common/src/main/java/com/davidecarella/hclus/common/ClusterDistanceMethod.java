package com.davidecarella.hclus.common;

/**
 * Un record che rappresenta un metodo per calcolare la distanza.
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
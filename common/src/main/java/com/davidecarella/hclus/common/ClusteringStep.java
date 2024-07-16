package com.davidecarella.hclus.common;

/**
 * Un passo del clustering.
 *
 * @param firstClusterIndex l'indice del primo cluster che viene unito
 * @param secondClusterIndex l'indice del second cluster che viene unito
 * @param newClusterSize la dimensione del nuovo cluster
 */
public record ClusteringStep(int firstClusterIndex, int secondClusterIndex, int newClusterSize) {
}

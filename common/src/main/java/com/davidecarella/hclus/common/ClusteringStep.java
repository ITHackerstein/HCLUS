package com.davidecarella.hclus.common;

/**
 * Record che rappresenta un passaggio avvenuto durante il processo di estrazione dei dati.
 *
 * @param firstClusterIndex l'indice del primo cluster che viene unito
 * @param secondClusterIndex l'indice del second cluster che viene unito
 * @param distance la distanza tra i due cluster
 * @param newClusterSize la dimensione del nuovo cluster
 */
public record ClusteringStep(int firstClusterIndex, int secondClusterIndex, double distance, int newClusterSize) {
}

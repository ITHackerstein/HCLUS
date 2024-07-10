package com.davidecarella.hclus.client.data;

import java.util.Set;

/**
 * Record che rappresenta un cluster ricevuto dal server.
 *
 * @param indices la lista di indici di esempi
 */
public record Cluster(Set<Integer> indices) {
}

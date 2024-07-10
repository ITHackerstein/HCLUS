package com.davidecarella.hclus.client.data;

import java.util.List;

/**
 * Record che rappresenta un dendrogramma ricevuto dal server.
 *
 * @param tree l'albero che contiene l'insieme di cluster
 */
public record Dendrogram(List<ClusterSet> tree) {
}

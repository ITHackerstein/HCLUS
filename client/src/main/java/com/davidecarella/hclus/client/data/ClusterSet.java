package com.davidecarella.hclus.client.data;

import java.util.List;

/**
 * Record che rappresenta un insieme di cluster ricevuto dal server.
 *
 * @param clusters la lista dei cluster
 */
public record ClusterSet(List<Cluster> clusters) {
}

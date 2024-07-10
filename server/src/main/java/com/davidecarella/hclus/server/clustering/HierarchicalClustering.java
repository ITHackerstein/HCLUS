package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.server.data.Data;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.exceptions.ClusterSetTooSmallException;
import com.davidecarella.hclus.server.exceptions.InvalidDepthException;
import com.davidecarella.hclus.server.exceptions.InvalidSizeException;


/**
 * Classe che si occupa di effettuare l'operazione di "mining", ovvero di creare il dendrogramma a partire dai dati
 * forniti
 */
public class HierarchicalClustering {
    /**
     * Costruttore di default nascosto per evitare che venga costruita la classe.
     */
    private HierarchicalClustering() {}

    /**
     * Crea il dendrogramma utilizzando i dati forniti da {@code data} e l'oggetto per calcolare la distanza
     * {@code distanceCalculator}, entrambi forniti come parametro.
     *
     * @param data i dati
     * @param distanceCalculator l'oggetto per calcolare la distanza
     * @param depth la profondità del dendrogramma
     * @throws InvalidDepthException quando la profondità del dendrogramma supera il numero di esempi in {@code data}
     * @throws InvalidSizeException quando ci sono due esempi con lunghezze diverse
     */
    public static Dendrogram mine(Data data, ClusterDistance distanceCalculator, int depth) throws InvalidDepthException, InvalidSizeException {
        if (depth <= 0 || depth > data.getNumberOfExamples()) {
            throw new InvalidDepthException("La profondità del dendrogramma deve essere al massimo pari al numero di esempi nel dataset");
        }

        var firstLevel = new ClusterSet(data.getNumberOfExamples());
        for (int i = 0; i < data.getNumberOfExamples(); i++) {
            var cluster = new Cluster();
            cluster.addData(i);
            firstLevel.add(cluster);
        }

        var dendrogram = new Dendrogram(depth);
        dendrogram.setClusterSet(firstLevel, 0);

        for (int level = 1; level < depth; ++level) {
            ClusterSet newLevel = null;
            try {
                newLevel = dendrogram.getClusterSet(level - 1).mergeClosestClusters(distanceCalculator, data);
            } catch (ClusterSetTooSmallException ignored) {
            }

            assert newLevel != null;
            dendrogram.setClusterSet(newLevel, level);
        }

        return dendrogram;
    }
}

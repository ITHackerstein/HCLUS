package com.davidecarella.hclus.server.clustering;

import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.ClusteringStep;
import com.davidecarella.hclus.common.exceptions.ExampleSizeMismatchException;
import com.davidecarella.hclus.server.distance.ClusterDistance;
import com.davidecarella.hclus.server.exceptions.InvalidClusterIndexException;
import com.davidecarella.hclus.server.exceptions.InvalidDepthException;
import com.davidecarella.hclus.common.serialization.DataDeserializer;
import com.davidecarella.hclus.common.serialization.DataSerializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Gestisce il clustering gerarchico provvedendo metodi per estrarre un clustering, salvarlo su un file e leggerlo
 * da un file.
 */
public class HierarchicalClustering {
    /**
     * Costruttore di default nascosto per evitare che venga costruita la classe.
     */
    private HierarchicalClustering() {}

    /**
     * Estrae un clustering di profondità massima {@code depth}, specificata come parametro, a partire da
     * {@code dataset}, specificato come parametro, e utilizzando come metodo per il calcolo della distanza fra cluster
     * {@code distanceCalculator}, specificato come parametro.
     *
     * @param dataset il dataset
     * @param distanceCalculator il metodo per il calcolo della distanza fra cluster
     * @param depth la profondità del clustering
     * @return il clustering estratto
     * @throws InvalidDepthException in caso {@code depth} non sia positiva o superi il numero di esempi del {@code dataset}
     * @throws ExampleSizeMismatchException in caso si provi a calcolare la distanza fra esempi di dimensione diversa
     */
    public static Clustering mine(Dataset dataset, ClusterDistance distanceCalculator, int depth) throws InvalidDepthException, ExampleSizeMismatchException {
        final int n = dataset.getExampleCount();

        if (depth <= 0 || depth > n) {
            throw new InvalidDepthException("La profondità del dendrogramma deve essere al massimo pari al numero di esempi nel dataset!");
        }

        // The cluster distance matrix delle distanze fra cluster (initially it contains example distances).
        var distancesBetweenClusters = dataset.computeDistanceMatrix();

        // The array of clustering steps
        var steps = new ClusteringStep[depth - 1];

        // An array which maps an index to its corresponding cluster index.
        var indexMap = new int[dataset.getExampleCount()];
        for (int i = 0; i < n; i++) {
            indexMap[i] = i;
        }

        for (int k = 0; k < depth - 1; ++k) {
            // Find the two clusters with minimum distance.
            var minDistance = Double.POSITIVE_INFINITY;
            int firstCluster = 0;
            int secondCluster = 0;

            for (int i = 0; i < n - 1; ++i) {
                if (indexMap[i] == -1) {
                    continue;
                }

                for (int j = i + 1; j < n; ++j) {
                    if (distancesBetweenClusters[i][j] < minDistance) {
                        minDistance = distancesBetweenClusters[i][j];
                        firstCluster = i;
                        secondCluster = j;
                    }
                }
            }

            var firstClusterSize = indexMap[firstCluster] < dataset.getExampleCount() ? 1 : steps[indexMap[firstCluster] - n].newClusterSize();
            var secondClusterSize = indexMap[secondCluster] < dataset.getExampleCount() ? 1 : steps[indexMap[secondCluster] - n].newClusterSize();

            steps[k] = new ClusteringStep(
                Math.min(indexMap[firstCluster], indexMap[secondCluster]),
                Math.max(indexMap[firstCluster], indexMap[secondCluster]),
                firstClusterSize + secondClusterSize
            );

            // Removes the first cluster from the map and replaces the second cluster with the new one.
            indexMap[firstCluster] = -1;
            indexMap[secondCluster] = n + k;

            for (int i = 0; i < n; ++i) {
                // Update the distances from the new cluster to every other cluster (except the ones we removed).

                if (indexMap[i] == -1 || indexMap[i] == n + k) {
                    continue;
                }

                var iSize = indexMap[i] < n ? 1 : steps[indexMap[i] - n].newClusterSize();
                distancesBetweenClusters[i][secondCluster] = distancesBetweenClusters[secondCluster][i] =
                    distanceCalculator.distance(
                        distancesBetweenClusters[i][firstCluster],
                        distancesBetweenClusters[i][secondCluster],
                        minDistance,
                        firstClusterSize,
                        secondClusterSize,
                        iSize
                    );

                // Sets the distances to the first cluster to +inf so that they are not considered when finding
                // the two clusters with minimum distance in the next step.
                if (i < firstCluster) {
                    distancesBetweenClusters[i][firstCluster] = distancesBetweenClusters[firstCluster][i] =
                        Double.POSITIVE_INFINITY;
                }
            }
        }

        return new Clustering(dataset.getExampleCount(), steps);
    }

    /**
     * Carica un clustering estratto su {@code dataset}, specificato come parametro, dal file con nome {@code fileName},
     * specificato come parametro.
     *
     * @param fileName il nome del file da cui caricare il clustering
     * @param dataset il dataset a cui fa riferimento il clustering
     * @return il clustering caricato dal file
     * @throws IOException in caso di errori di I/O durante la lettura dal file
     * @throws InvalidDepthException in caso {@code depth} non sia positiva o superi il numero di esempi del {@code dataset}
     * @throws InvalidClusterIndexException in caso degli indici dei cluster non siano validi
     */
    public static Clustering load(String fileName, Dataset dataset) throws IOException, InvalidDepthException, InvalidClusterIndexException {
        try (var fileInputStream = new FileInputStream(fileName);
             var dataDeserializer = new DataDeserializer(fileInputStream))
        {
            var depth = dataDeserializer.deserializeInt();
            if (depth <= 0 || depth > dataset.getExampleCount()) {
                throw new InvalidDepthException("La profondità del dendrogramma deve essere al massimo pari al numero di esempi nel dataset!");
            }

            var steps = new ClusteringStep[depth - 1];
            for (int i = 0; i < depth - 1; ++i) {
                var step = dataDeserializer.deserializeClusteringStep();
                // NOTE: Two cluster cannot be joined if they haven't been generated yet.
                if (step.firstClusterIndex() < 0 || step.firstClusterIndex() > i + dataset.getExampleCount() - 1 ||
                    step.secondClusterIndex() < 0 || step.secondClusterIndex() > i + dataset.getExampleCount() - 1)
                {
                    throw new InvalidClusterIndexException("Indice del cluster non valido!");
                }
                steps[i] = step;
            }

            return new Clustering(dataset.getExampleCount(), steps);
        }
    }

    /**
     * Salva {@code clustering}, specificato come parametro, nel file con nome {@code fileName}, specificato come
     * parametro.
     *
     * @param clustering il clustering che si vuole salvare sul file
     * @param fileName il file su cui salvare il clustering
     * @throws IOException in caso di errori di I/O durante la scrittura sul file
     */
    public static void save(Clustering clustering, String fileName) throws IOException {
        try (var fileOutputStream = new FileOutputStream(fileName);
             var dataSerializer = new DataSerializer(fileOutputStream))
        {
            dataSerializer.serializeInt(clustering.steps().length + 1);
            for (var step : clustering.steps()) {
                dataSerializer.serializeClusteringStep(step);
            }
        }
    }
}

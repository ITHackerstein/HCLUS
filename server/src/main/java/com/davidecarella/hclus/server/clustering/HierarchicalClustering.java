package com.davidecarella.hclus.server.clustering;

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
 * Classe che si occupa di effettuare l'operazione di estrazione, ovvero di creare il dendrogramma a partire dai dati
 * forniti.
 */
public class HierarchicalClustering {
    /**
     * Costruttore di default nascosto per evitare che venga costruita la classe.
     */
    private HierarchicalClustering() {}

    /**
     * Crea il dendrogramma utilizzando il dataset ({@code dataset}), l'oggetto per calcolare la distanza
     * ({@code distanceCalculator}) e la profondità ({@code depth}), forniti come parametro.
     *
     * @param dataset il dataset
     * @param distanceCalculator l'oggetto per calcolare la distanza
     * @param depth la profondità del dendrogramma
     * @throws InvalidDepthException quando la profondità del dendrogramma supera il numero di esempi in {@code data}
     * @throws ExampleSizeMismatchException quando ci sono due esempi con lunghezze diverse
     */
    public static ClusteringStep[] mine(Dataset dataset, ClusterDistance distanceCalculator, int depth) throws InvalidDepthException, ExampleSizeMismatchException {
        final int n = dataset.getNumberOfExamples();

        if (depth <= 0 || depth > n) {
            throw new InvalidDepthException("La profondità del dendrogramma deve essere al massimo pari al numero di esempi nel dataset");
        }

        var distancesBetweenClusters = dataset.computeDistanceMatrix();
        var steps = new ClusteringStep[depth - 1];
        var indexMap = new int[dataset.getNumberOfExamples()];
        for (int i = 0; i < n; i++) {
            indexMap[i] = i;
        }

        for (int k = 0; k < depth - 1; ++k) {
            var minDistance = Double.MAX_VALUE;
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

            var firstClusterSize = indexMap[firstCluster] < dataset.getNumberOfExamples() ? 1 : steps[indexMap[firstCluster] - n].newClusterSize();
            var secondClusterSize = indexMap[secondCluster] < dataset.getNumberOfExamples() ? 1 : steps[indexMap[secondCluster] - n].newClusterSize();

            steps[k] = new ClusteringStep(
                Math.min(indexMap[firstCluster], indexMap[secondCluster]),
                Math.max(indexMap[firstCluster], indexMap[secondCluster]),
                minDistance,
                firstClusterSize + secondClusterSize
            );

            indexMap[firstCluster] = -1;
            indexMap[secondCluster] = n + k;

            for (int i = 0; i < n; ++i) {
                if (indexMap[i] == -1 || indexMap[i] == n + k) {
                    continue;
                }

                var iSize = indexMap[i] < n ? 1 : steps[indexMap[i] - n].newClusterSize();
                distancesBetweenClusters[i][secondCluster] = distanceCalculator.distance(
                    distancesBetweenClusters[i][firstCluster],
                    distancesBetweenClusters[i][secondCluster],
                    minDistance,
                    firstClusterSize,
                    secondClusterSize,
                    iSize
                );

                if (i < firstCluster) {
                    distancesBetweenClusters[i][firstCluster] = Double.MAX_VALUE;
                }
            }
        }

        return steps;
    }

    public static ClusteringStep[] load(String fileName, Dataset dataset) throws IOException, InvalidDepthException, InvalidClusterIndexException {
        try (var fileInputStream = new FileInputStream(fileName);
             var dataDeserializer = new DataDeserializer(fileInputStream))
        {
            var depth = dataDeserializer.deserializeInt();
            if (depth <= 0) {
                throw new InvalidDepthException("Profondità non valida!");
            }

            var steps = new ClusteringStep[depth - 1];
            for (int i = 0; i < depth - 1; ++i) {
                var step = dataDeserializer.deserializeClusteringStep();
                // NOTE: Two cluster cannot be joined if they haven't been generated yet.
                if (step.firstClusterIndex() < 0 || step.firstClusterIndex() > i + dataset.getNumberOfExamples() - 1 ||
                    step.secondClusterIndex() < 0 || step.secondClusterIndex() > i + dataset.getNumberOfExamples() - 1)
                {
                    throw new InvalidClusterIndexException("Indice del cluster non valido!");
                }
                steps[i] = step;
            }

            return steps;
        }
    }

    public static void save(ClusteringStep[] clusteringSteps, String fileName) throws IOException {
        try (var fileOutputStream = new FileOutputStream(fileName);
             var dataSerializer = new DataSerializer(fileOutputStream))
        {
            dataSerializer.serializeInt(clusteringSteps.length + 1);
            for (var step : clusteringSteps) {
                dataSerializer.serializeClusteringStep(step);
            }
        }
    }
}

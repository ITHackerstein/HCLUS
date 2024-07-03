package com.davidecarella.distance;

import com.davidecarella.clustering.Cluster;
import com.davidecarella.data.Data;
import com.davidecarella.exceptions.InvalidSizeException;

/**
 * <p>Implementazione di {@link ClusterDistance} per il calcolo della distanza average-link tra due cluster.
 *
 * <p>Viene calcolata come:
 * $$\large{\frac{\displaystyle \sum_{\substack{t_1 \in C_1 \\ t_2 \in C_2}} d(t_1, t_2)}{|C_1 \times C_2|}}$$
 * dove \(C_1, C_2\) sono i due cluster.
 */
public class AverageLinkDistance implements ClusterDistance {
    /**
     * Calcola la distanza tra i {@link Cluster cluster} {@code c1} e {@code c2}, specificati come parametri,
     * usando {@code data}, specificata come parametro, per i valori degli esempi.
     *
     * @param c1 il primo {@link Cluster cluster} su cui calcolare la distanza
     * @param c2 il second {@link Cluster cluster} su cui calcolare la distanza
     * @param data i dati
     * @return la distanza tra i {@link Cluster cluster} {@code c1} e {@code c2}
     * @throws InvalidSizeException se degli esempi nei {@link Cluster cluster} hanno lunghezze diverse
     */
    @Override
    public double distance(Cluster c1, Cluster c2, Data data) throws InvalidSizeException {
        double sum = 0.0;

        var firstIterator = c1.iterator();
        while (firstIterator.hasNext()) {
            var firstExample = data.getExample(firstIterator.next());

            var secondIterator = c2.iterator();
            while (secondIterator.hasNext()) {
                var secondExample = data.getExample(secondIterator.next());

                sum +=firstExample.distance(secondExample);
            }
        }

        return sum / (c1.getSize() * c2.getSize());
    }
}

package com.davidecarella;

/**
 * <p>Implementazione di {@link ClusterDistance} per il calcolo della distanza average-link tra due cluster.
 *
 * <p>Viene calcolata come:
 * $$\large{\frac{\displaystyle \sum_{\substack{t_1 \in C_1 \\ t_2 \in C_2}} d(t_1, t_2)}{|C_1 \times C_2|}}$$
 * dove \(C_1, C_2\) sono i due cluster.
 */
class AverageLinkDistance implements ClusterDistance {
    /**
     * Calcola la distanza average-link tra i cluster {@code c1} e {@code c2}, specificati come parametri.
     *
     * @param c1 il primo cluster su cui calcolare la distanza
     * @param c2 il second cluster su cui calcolare la distanza
     * @param data i dati
     * @return la distanza average-link tra i cluster {@code c1} e {@code c2}
     */
    @Override
    public double distance(Cluster c1, Cluster c2, Data data) {
        double sum = 0.0;

        for (int i = 0; i < c1.getSize(); ++i) {
            for (int j = 0; j < c2.getSize(); ++j) {
                var firstExample = data.getExample(c1.getElement(i));
                var secondExample = data.getExample(c2.getElement(j));

                sum += firstExample.distance(secondExample);
            }
        }

        return sum / (c1.getSize() * c2.getSize());
    }
}

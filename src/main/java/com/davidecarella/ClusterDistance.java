package com.davidecarella;

/**
 * <p>Interfaccia funzionale per il calcolo generico della distanza tra due cluster.
 *
 * <p>Le due metriche fornite dal progetto sono
 * <ul>
 *     <li>{@link SingleLinkDistance}</li>
 *     <li>{@link AverageLinkDistance}</li>
 * </ul>
 */
interface ClusterDistance {
    /**
     * Calcola la distanza tra i cluster {@code c1} e {@code c2}, specificati come parametri, usando {@code data},
     * specificata come parametro, per i valori degli esempi
     *
     * @param c1 il primo cluster su cui calcolare la distanza
     * @param c2 il second cluster su cui calcolare la distanza
     * @param data i dati
     * @return la distanza tra i cluster {@code c1} e {@code c2}
     */
    double distance(Cluster c1, Cluster c2, Data data);
}

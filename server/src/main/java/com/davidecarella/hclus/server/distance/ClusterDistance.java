package com.davidecarella.hclus.server.distance;

import com.davidecarella.hclus.server.clustering.Cluster;
import com.davidecarella.hclus.server.data.Data;
import com.davidecarella.hclus.server.exceptions.InvalidSizeException;

/**
 * <p>Interfaccia funzionale per il calcolo generico della distanza tra due {@link Cluster cluster}.
 *
 * <p>Le due metriche fornite dal progetto sono
 * <ul>
 *     <li>{@link SingleLinkDistance}</li>
 *     <li>{@link AverageLinkDistance}</li>
 * </ul>
 */
public interface ClusterDistance {
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
    double distance(Cluster c1, Cluster c2, Data data) throws InvalidSizeException;
}

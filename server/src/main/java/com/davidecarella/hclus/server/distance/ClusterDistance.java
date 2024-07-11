package com.davidecarella.hclus.server.distance;

/**
 * <p>Interfaccia funzionale per il calcolo generico della distanza tra due cluster.
 *
 * <p>Le due metriche fornite dal progetto sono
 * <ul>
 *     <li>{@link SingleLinkDistance}</li>
 *     <li>{@link AverageLinkDistance}</li>
 * </ul>
 */
public interface ClusterDistance {
    /**
     * Calcola la distanza tra il cluster \(i\) e il nuovo cluster \(xy\) ottenuto unendo i cluster \(x\) e \(y\).
     *
     * @param dxi la distanza tra il cluster \(x\) e il cluster \(i\)
     * @param dyi la distanza tra il cluster \(y\) e il cluster \(i\)
     * @param dxy la distanza tra il cluster \(x\) e il cluster \(y\)
     * @param xSize la dimensione del cluster \(x\)
     * @param ySize la dimensione del cluster \(y\)
     * @param iSize la dimensione del cluster \(i\)
     */
    double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize);
}

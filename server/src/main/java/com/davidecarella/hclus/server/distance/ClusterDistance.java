package com.davidecarella.hclus.server.distance;

/**
 * Interfaccia funzionale per il calcolo della distanza fra cluster.
 */
public interface ClusterDistance {
    /**
     * Considerati i cluster \(X\), \(Y\) e \(I\), calcola la distanza tra il cluster \(X \cup Y\) e \(I\).
     *
     * @param dxi la distanza tra il cluster \(X\) e il cluster \(I\)
     * @param dyi la distanza tra il cluster \(Y\) e il cluster \(I\)
     * @param dxy la distanza tra il cluster \(X\) e il cluster \(Y\)
     * @param xSize la dimensione del cluster \(X\)
     * @param ySize la dimensione del cluster \(Y\)
     * @param iSize la dimensione del cluster \(I\)
     * @return la distanza tra il cluster \(X \cup Y\) e \(I\)
     */
    double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize);

    /**
     * Restituisce il nome del metodo per il calcolo della distanza fra cluster.
     *
     * @return il nome del metodo
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}

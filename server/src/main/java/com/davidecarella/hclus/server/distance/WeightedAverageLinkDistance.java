package com.davidecarella.hclus.server.distance;

/**
 * <p>Considerati tre cluster \(X\), \(Y\) e \(I\) assegna alla distanza \(d(X \cup Y, I)\) la media della distanza
 * tra i cluster \(X\) e \(I\) e la distanza tra i cluster \(Y\) e \(I\).
 *
 * <p>In formule:
 * $$d(X \cup Y, I) = \frac{d(X, I) + d(Y, I)}{2}$$
 */
public class WeightedAverageLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public WeightedAverageLinkDistance() {}

    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return 0.5 * (dxi + dyi);
    }

    @Override
    public String getName() {
        return "Weighted-Average-Link";
    }
}

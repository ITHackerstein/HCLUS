package com.davidecarella.hclus.server.distance;

/**
 * <p>Implementazione di {@link ClusterDistance} per il calcolo della distanza average-link tra due cluster.
 *
 * <p>Viene calcolata come:
 * $$\large{\frac{\displaystyle \sum_{\substack{t_1 \in C_1 \\ t_2 \in C_2}} d(t_1, t_2)}{|C_1 \times C_2|}}$$
 * dove \(C_1, C_2\) sono i due cluster.
 */
public class AverageLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public AverageLinkDistance() {}

    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return (dxi * xSize + dyi * ySize) / (xSize + ySize);
    }

    @Override
    public String getName() {
        return "Average-Link";
    }
}

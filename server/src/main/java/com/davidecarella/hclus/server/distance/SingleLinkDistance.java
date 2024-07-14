package com.davidecarella.hclus.server.distance;

/**
 * <p>Implementazione di {@link ClusterDistance} per il calcolo della distanza single-link tra due cluster. calcolata come:
 *
 * <p>Viene calcolata come
 * $$\large{\min_{\substack{t_1 \in C_1 \\ t_2 \in C_2}} d(t_1, t_2)}$$
 * dove \(C_1, C_2\) sono i due cluster.
 */
public class SingleLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public SingleLinkDistance() {}

    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.min(dxi, dyi);
    }

    @Override
    public String getName() {
        return "Single-Link";
    }
}

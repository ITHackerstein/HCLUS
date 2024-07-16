package com.davidecarella.hclus.server.distance;

/**
 * Calcola la distanza utilizzando il metodo di Ward.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Ward's_method">Metodo di Ward</a>
 */
public class WardLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public WardLinkDistance() {}

    /**
     * {@inheritDoc}
     * Utilizza la formula di la formula di Lance-Williams.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Ward's_method#Lance%E2%80%93Williams_algorithms">Formula di Lance-Williams</a>
     */
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        double denominator = 1.0 / (xSize + ySize + iSize);
        return Math.sqrt(
            (xSize + iSize) * denominator * dxi * dxi +
            (ySize + iSize) * denominator * dyi * dyi -
            iSize * denominator * dxy * dxy
        );
    }

    @Override
    public String getName() {
        return "Ward-Link";
    }
}

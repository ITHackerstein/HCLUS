package com.davidecarella.hclus.server.distance;

public class MedianLinkDistance implements ClusterDistance {
    // FIXME: Document the derivation of the formula
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.sqrt(0.5 * (dxi * dxi + dyi * dyi) - 0.25 * dxy * dxy);
    }

    @Override
    public String getName() {
        return "Median-Link";
    }
}

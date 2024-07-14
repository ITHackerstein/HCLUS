package com.davidecarella.hclus.server.distance;

public class WardLinkDistance implements ClusterDistance {
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

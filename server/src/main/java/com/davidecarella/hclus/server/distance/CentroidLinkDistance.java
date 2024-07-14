package com.davidecarella.hclus.server.distance;

public class CentroidLinkDistance implements ClusterDistance {
    // FIXME: Document the derivation of the formula
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.sqrt((xSize * dxi * dxi + ySize * dyi * dyi - (xSize * ySize * dxy * dxy) / (xSize + ySize)) / (xSize + ySize));
    }

    @Override
    public String getName() {
        return "Centroid-Link";
    }
}

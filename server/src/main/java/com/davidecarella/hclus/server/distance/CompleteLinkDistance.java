package com.davidecarella.hclus.server.distance;

public class CompleteLinkDistance implements ClusterDistance {
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.max(dxi, dyi);
    }

    @Override
    public String getName() {
        return "Complete-Link";
    }
}

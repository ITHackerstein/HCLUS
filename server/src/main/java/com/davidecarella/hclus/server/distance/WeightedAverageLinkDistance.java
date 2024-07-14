package com.davidecarella.hclus.server.distance;

public class WeightedAverageLinkDistance implements ClusterDistance {
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return 0.5 * (dxi + dyi);
    }

    @Override
    public String getName() {
        return "Weighted-Average-Link";
    }
}

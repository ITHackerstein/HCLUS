package com.davidecarella.hclus.server.distance;

/**
 * <p>Considerati due cluster \(X\) e \(Y\) ne calcola la distanza considerando la minima tra le distanze degli esempi
 * in \(X\) e in \(Y\).
 *
 * <p>In formule:
 * $$d(X, Y) = \min_{\substack{x \in X \\ y \in Y}} \lVert x - y \rVert_2^2$$
 */
public class SingleLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public SingleLinkDistance() {}

    /**
     * {@inheritDoc}
     * <br>
     * Possiamo derivare la formula, sapendo che
     * $$
     * \begin{aligned}
     * d(X, I) &amp;= \min_{\substack{x \in X \\ i \in I}} \lVert x - i \rVert_2^2 \\
     * d(Y, I) &amp;= \min_{\substack{y \in Y \\ i \in I}} \lVert y - i \rVert_2^2 \\
     * \end{aligned}
     * $$
     * da cui segue che
     * $$
     * \begin{aligned}
     * d(X \cup Y, I) &amp;= \min_{\substack{v \in X \cup Y \\ i \in I}} \lVert v - i \rVert_2^2 \\
     * &amp;= \min\left\{\min_{\substack{x \in X \\ i \in I}} \lVert x - i \rVert_2^2, \min_{\substack{y \in Y \\ i \in I}} \lVert y - i \rVert_2^2\right\}
     * \end{aligned}
     * $$
     */
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.min(dxi, dyi);
    }

    @Override
    public String getName() {
        return "Single-Link";
    }
}

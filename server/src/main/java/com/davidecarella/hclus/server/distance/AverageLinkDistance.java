package com.davidecarella.hclus.server.distance;

/**
 * <p>Considerati due cluster \(X\) e \(Y\) ne calcola la distanza considerando la media di tutte le distanze fra gli
 * esempi in \(X\) e in \(Y\).
 *
 * <p>In formule:
 * $$d(X, Y) = \frac{1}{|X| \cdot |Y|} \sum_{x \in X} \sum_{x \in X} \lVert x - y \rVert_2$$
 */
public class AverageLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public AverageLinkDistance() {}

    /**
     * {@inheritDoc}<br>
     * Possiamo derivare la formula, sapendo che
     * $$
     * \begin{aligned}
     * d(X, I) &amp;= \frac{1}{|X| \cdot |I|} \sum_{x \in X} \sum_{i \in I} \lVert x - i \rVert_2 \\
     * d(Y, I) &amp;= \frac{1}{|Y| \cdot |I|} \sum_{y \in Y} \sum_{i \in I} \lVert y - i \rVert_2
     * \end{aligned}
     * $$
     * da cui segue che
     * $$
     * \begin{aligned}
     * d(X \cup Y, I) &amp;= \frac{1}{(|X| + |Y|) \cdot |I|} \sum_{v \in X \cup Y} \sum_{i \in I} \lVert v - i \rVert_2 = \\
     * &amp;= \frac{1}{(|X| + |Y|) \cdot |I|} \left[ \sum_{x \in X} \sum_{i \in I} \lVert x - i \rVert_2 + \sum_{y \in Y} \sum_{i \in I} \lVert y - i \rVert_2 \right] = \\
     * &amp;= \frac{1}{(|X| + |Y|)} \left[ |X| \cdot d(X, I) + |Y| \cdot d(Y, I) \right]
     * \end{aligned}
     * $$
     */
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return (dxi * xSize + dyi * ySize) / (xSize + ySize);
    }

    @Override
    public String getName() {
        return "Average-Link";
    }
}
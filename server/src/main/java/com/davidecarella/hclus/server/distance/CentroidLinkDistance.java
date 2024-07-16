package com.davidecarella.hclus.server.distance;

/**
 * <p>Considerati due cluster \(X\) e \(Y\) ne calcola la distanza considerando la distanza euclidea fra i loro due
 * baricentri.
 *
 * <p>In formule, possiamo trovare il baricentro di un cluster \(X\) con la seguente formula
 * $$
 * C_X = \frac{1}{|X|} \sum_{x \in X} x
 * $$
 * e dunque
 * $$
 * d(X, Y) = \lVert C_X - C_Y \rVert_2
 * $$
 */
public class CentroidLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public CentroidLinkDistance() {}

    /**
     * {@inheritDoc}<br>
     * Possiamo derivare la formula, sapendo che
     * $$
     * \begin{aligned}
     * d(X, I) &amp;= \lVert C_X - C_I \rVert_2 \\
     * d(Y, I) &amp;= \lVert C_Y - C_I \rVert_2 \\
     * d(X, Y) &amp;= \lVert C_X - C_Y \rVert_2
     * \end{aligned}
     * $$
     * inoltre, notiamo che, i vettori delle distanze \(C_X - C_I\), \(C_Y - C_I\) e \(C_X - C_Y\) formano un triangolo
     * e, per il teorema del coseno, vale che
     * $$
     * \lVert C_X - C_Y \rVert_2^2 =
     * \lVert C_X - C_I \rVert_2^2 + \lVert C_Y - C_I \rVert_2^2 - 2 \lVert C_X - C_I \rVert \cdot \lVert C_Y - C_I \rVert \cos \theta =
     * \lVert C_X - C_I \rVert_2^2 + \lVert C_Y - C_I \rVert_2^2 - 2 (C_X - C_I) \cdot (C_Y - C_I)
     * $$
     * Dunque vale che
     * $$
     * \begin{aligned}
     * d(X \cup Y, I) &amp;= \lVert C_{X \cup Y} - C_I \rVert_2 = \\
     * &amp;= \lVert \frac{|X| C_X + |Y| C_Y}{|X| + |Y|} - C_I \rVert_2 = \\
     * &amp;= \lVert \frac{|X| (C_X - C_I) + |Y| (C_Y - C_I)}{|X| + |Y|} \rVert_2 = \\
     * &amp;= \sqrt{\frac{|X|^2 \lVert C_X - C_I \rVert_2^2 + |Y|^2 \lVert C_Y - C_I \rVert_2^2 + 2 |X| |Y| (C_X - C_I) (C_Y - C_I)}{(|X| + |Y|)^2}} = \\
     * &amp;= \sqrt{\frac{|X|^2 \lVert C_X - C_I \rVert_2^2 + |Y|^2 \lVert C_Y - C_I \rVert_2^2 + |X| |Y| \lVert C_X - C_I \rVert_2^2 + |X| |Y| \lVert C_Y - C_I \rVert_2^2 - |X| |Y| \lVert C_X - C_Y \rVert_2^2}{(|X| + |Y|)^2}} = \\
     * &amp;= \sqrt{\frac{|X| (|X| + |Y|) \lVert C_X - C_I\rVert_2^2 + |Y| (|X| + |Y|) \lVert C_Y - C_I\rVert_2^2 - |X| |Y| \lVert C_X - C_Y \rVert_2^2}{(|X| + |Y|)^2}} = \\
     * &amp;= \sqrt{\frac{|X| \lVert C_X - C_I \rVert_2^2 + |Y| \lVert C_Y - C_I \rVert_2^2}{|X| + |Y|} - \frac{|X| |Y| \lVert C_X - C_Y \rVert_2^2}{(|X| + |Y|)^2}} = \\
     * &amp;= \sqrt{\frac{1}{|X| + |Y|} \left(|X| \lVert C_X - C_I \rVert_2^2 + |Y| \lVert C_Y - C_I \rVert_2^2 - \frac{1}{|X| + |Y|} |X| |Y| \lVert C_X - C_Y \rVert_2^2\right)}
     * \end{aligned}
     * $$
     */
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.sqrt((xSize * dxi * dxi + ySize * dyi * dyi - (xSize * ySize * dxy * dxy) / (xSize + ySize)) / (xSize + ySize));
    }

    @Override
    public String getName() {
        return "Centroid-Link";
    }
}

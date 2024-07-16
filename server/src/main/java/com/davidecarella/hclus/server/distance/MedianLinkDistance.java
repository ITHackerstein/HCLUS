package com.davidecarella.hclus.server.distance;

/**
 * <p>Considerati tre cluster \(X\), \(Y\) e \(I\) assegna la distanza \(d(X \cup Y, I)\) come il metodo
 * {@link CentroidLinkDistance} con l'unica differenza che viene considerata la media dei baricentri di \(X\) e \(Y\).
 *
 * <p>In formule:
 * $$d(X \cup Y, I) = \left\lVert \frac{1}{2} (C_X + C_Y) - C_I \right\rVert_2$$
 */
public class MedianLinkDistance implements ClusterDistance {
    /**
     * Costruttore di default.
     */
    public MedianLinkDistance() {}

    /**
     * {@inheritDoc}<br>
     * Possiamo derivare la formula, notando che i vettori distanza \(C_X - C_I\), \(C_Y - C_I\) e \(C_X - C_Y\) formano
     * un triangolo e che, per il teorema del coseno, si ha che
     * $$
     * \lVert C_X - C_Y \rVert_2^2 =
     * \lVert C_X - C_I \rVert_2^2 + \lVert C_Y - C_I \rVert_2^2 - 2 \lVert C_X - C_I \rVert \cdot \lVert C_Y - C_I \rVert \cos \theta =
     * \lVert C_X - C_I \rVert_2^2 + \lVert C_Y - C_I \rVert_2^2 - 2 (C_X - C_I) \cdot (C_Y - C_I)
     * $$
     * da cui segue che
     * $$
     * \begin{aligned}
     * d(X \cup Y, I) &amp;= \left\lVert \frac{1}{2} (C_X + C_Y) - C_I \right\rVert_2 = \\
     * &amp;= \left\lVert \frac{1}{2} (C_X - C_I) + \frac{1}{2} (C_Y - C_I) \right\rVert_2 = \\
     * &amp;= \sqrt{\frac{1}{4} \lVert C_X - C_I \rVert_2^2 + \frac{1}{4} \lVert C_Y - C_I \rVert_2^2 + \frac{1}{2} (C_X - C_I) \cdot (C_Y - C_I)} = \\
     * &amp;= \sqrt{\frac{1}{4} \lVert C_X - C_I \rVert_2^2 + \frac{1}{4} \lVert C_Y - C_I \rVert_2^2 + \frac{1}{4} \lVert C_X - C_I \rVert_2^2 + \frac{1}{4} \lVert C_Y - C_I \rVert_2^2 - \frac{1}{4} \lVert C_X - C_Y \rVert_2^2} = \\
     * &amp;= \sqrt{\frac{1}{2} \lVert C_X - C_I \rVert_2^2 + \frac{1}{2} \lVert C_Y - C_I \rVert_2^2 - \frac{1}{4} \lVert C_X - C_Y \rVert_2^2} = \\
     * &amp;= \sqrt{\frac{1}{2} (\lVert C_X - C_I \rVert_2^2 + \lVert C_Y - C_I \rVert_2^2) - \frac{1}{4} \lVert C_X - C_Y \rVert_2^2}
     * \end{aligned}
     * $$
     */
    @Override
    public double distance(double dxi, double dyi, double dxy, int xSize, int ySize, int iSize) {
        return Math.sqrt(0.5 * (dxi * dxi + dyi * dyi) - 0.25 * dxy * dxy);
    }

    @Override
    public String getName() {
        return "Median-Link";
    }
}

package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.common.Clustering;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * <p>Il dendrogramma associato a un clustering ottenuto dal server.
 *
 * <p>Viene utilizzata per mostrare un albero i cui nodi rappresentano i cluster generati in ogni passo dal clustering,
 * in particolare questa genera le posizioni dei nodi, le connessioni fra loro e di alcune costanti per la loro
 * visualizzazione con {@link DendrogramViewerWidget}.
 *
 * <p>Nello specifico l'array delle posizioni dei nodi è un array la cui dimensione è la somma tra numero di esempi,
 * sia questo \(E\), e il numero di passaggi del clustering, sia questo \(P\). Le prime \(E\) posizioni fanno
 * riferimento alle foglie del dendrogramma e le successive, invece, fanno riferimento a ciascun cluster generato
 * in ogni passo del clustering.
 *
 * <p>Le connessioni, invece, sono rappresentate con una matrice di dimensione \(2P \times 2\) infatti ogni cluster
 * non-foglia possiede due connessioni e per ciascuna di essa memorizziamo gli indici del cluster di partenza e di
 * arrivo.
 *
 * @see DendrogramViewerWidget
 */
class Dendrogram {
    /**
     * L'orientazione del dendrogramma.
     */
    enum Orientation {
        /**
         * Orientazione verticale.
         */
        VERTICAL,
        /**
         * Orientazione orizzontale.
         */
        HORIZONTAL
    }

    /**
     * La dimensione (in pixel) di un cluster.
     */
    static final int CLUSTER_SIZE = 16;

    /**
     * Lo spazio (in pixel) tra un cluster e l'altro.
     */
    static final int CLUSTER_SPACING = 8;

    /**
     * La dimensione (in pixel) del bordo di un cluster.
     */
    static final int CLUSTER_BORDER_SIZE = 2;

    /**
     * La larghezza (in pixel) di una connessione tra cluster.
     */
    static final int EDGE_LINE_WIDTH = 1;

    /**
     * Il colore di un cluster.
     */
    static final Color CLUSTER_COLOR = new Color(138, 43, 216);

    /**
     * Il colore della connessione tra cluster.
     */
    static final Color EDGE_COLOR = Color.black;

    /**
     * Il colore dello sfondo del tooltip di informazioni su un cluster.
     */
    static final Color TOOLTIP_BACKGROUND_COLOR = new Color(200, 200, 200);

    /**
     * Il padding (in pixel) del tooltip di informazioni su un cluster.
     */
    static final int TOOLTIP_PADDING = 5;

    /**
     * La dimensione (in pixel) del bordo del tooltip di informazioni su un cluster.
     */
    static final int TOOLTIP_BORDER_SIZE = 2;

    /**
     * L'altezza (in percentuale) del tooltip di informazioni su un cluster.
     */
    static final double TOOLTIP_HEIGHT_PERCENTAGE = 0.2;

    /**
     * L'altezza (in pixel) della scrollbar del tooltip di informazioni su un cluster.
     */
    static final int TOOLTIP_SCROLLBAR_HEIGHT = 12;

    /**
     * La larghezza (in pixel) della scrollbar del tooltip di informazioni su un cluster.
     */
    static final int TOOLTIP_SCROLLBAR_WIDTH = 2;

    /**
     * Il clustering a cui fa riferimento il dendrogramma.
     */
    private final Clustering clustering;

    /**
     * L'orientazione del dendrogramma.
     */
    private Orientation orientation = Orientation.VERTICAL;

    /**
     * Le posizioni dei cluster del dendrogramma.
     */
    private final Point[] clusterPositions;

    /**
     * Le connessioni fra cluster.
     */
    private final int[][] edges;

    /**
     * Costruisce un dendrogramma che fa riferimento a {@code clustering}, specificato come parametro.
     *
     * @param clustering il clustering in base al quale si vuole costruire un dendrogramma
     */
    Dendrogram(Clustering clustering) {
        this.clustering = clustering;
        this.clusterPositions = new Point[this.clustering.exampleCount() + this.clustering.steps().length];
        this.edges = new int[this.clustering.steps().length * 2][2];
        this.initializeArrays();
    }

    /**
     * Inizializza gli array utilizzati per la visualizzazione del dendrogramma.
     */
    private void initializeArrays() {
        for (int clusterIndex = 0; clusterIndex < this.clustering.exampleCount(); ++clusterIndex) {
            this.clusterPositions[clusterIndex] = new Point((CLUSTER_SPACING + CLUSTER_SIZE * 2) * (clusterIndex - this.clustering.exampleCount() / 2), 0);
        }

        this.reorderLeafs();

        int edgeIndex = 0;
        for (int i = 0; i < this.clustering.steps().length; ++i) {
            var step = this.clustering.steps()[i];
            var firstClusterPosition = this.clusterPositions[step.firstClusterIndex()];
            var secondClusterPosition = this.clusterPositions[step.secondClusterIndex()];
            this.clusterPositions[this.clustering.exampleCount() + i] = new Point(
                (int) ((firstClusterPosition.getX() + secondClusterPosition.getX()) / 2),
                (int) (Math.min(firstClusterPosition.getY(), secondClusterPosition.getY()) - CLUSTER_SPACING - CLUSTER_SIZE * 2)
            );

            this.edges[edgeIndex][0] = this.clustering.exampleCount() + i;
            this.edges[edgeIndex][1] = step.firstClusterIndex();
            ++edgeIndex;
            this.edges[edgeIndex][0] = this.clustering.exampleCount() + i;
            this.edges[edgeIndex][1] = step.secondClusterIndex();
            ++edgeIndex;
        }
    }

    /**
     * Riordina le foglie del dendrogramma in modo tale che il dendrogramma non contenga nodi o connessioni che si
     * sovrappongono.
     */
    private void reorderLeafs() {
        int[] order;
        if (this.clustering.exampleCount() == this.clustering.steps().length + 1) {
            order = findLeafOrderForCompleteDendrogram();
        } else {
            order = findLeafOrderForPartialDendrogram();
        }

        var oldClusterPositions = Arrays.copyOf(this.clusterPositions, this.clusterPositions.length);
        for (int i = 0; i < this.clusterPositions.length; ++i) {
            if (i < order.length) {
                this.clusterPositions[order[i]] = oldClusterPositions[i];
            } else {
                this.clusterPositions[i] = oldClusterPositions[i];
            }
        }
    }

    /**
     * <p>Trova l'ordine delle foglie per un dendrogramma completo (con profondità massima).
     *
     * <p>Considerato l'array delle posizioni dei cluster e una coppia {@code (i, j)}, dove {@code i} è un indice
     * nell'array di ordinamento restituito e {@code j} è il valore in posizione {@code i}, allora si avrà che nel
     * nuovo array di posizioni dei cluster {@code A[j] = A[i]}.
     *
     * @return l'array di ordinamento delle foglie
     */
    private int[] findLeafOrderForCompleteDendrogram() {
        var stack = new Stack<Integer>();
        stack.push(this.clusterPositions.length - 1);

        var order = new int[this.clustering.exampleCount()];
        int orderIndex = 0;

        while (!stack.empty()) {
            int clusterIndex = stack.pop();
            if (clusterIndex < this.clustering.exampleCount()) {
                order[orderIndex++] = clusterIndex;
                continue;
            }

            pushClusterChildrenInOrder(stack, clusterIndex);
        }

        return order;
    }

    /**
     * <p>Trova l'ordine delle foglie per un dendrogramma completo (con profondità minore di quella massima).
     *
     * <p>Considerato l'array delle posizioni dei cluster e una coppia {@code (i, j)}, dove {@code i} è un indice
     * nell'array di ordinamento restituito e {@code j} è il valore in posizione {@code i}, allora si avrà che nel
     * nuovo array di posizioni dei cluster {@code A[j] = A[i]}.
     *
     * @return l'array di ordinamento delle foglie
     */
    private int[] findLeafOrderForPartialDendrogram() {
        var stack = new Stack<Integer>();
        for (int i = 0; i < this.clustering.steps().length; ++i) {
            stack.push(this.clustering.exampleCount() + i);
        }

        stack.sort((a, b) -> {
            var aSize = this.clustering.steps()[a - this.clustering.exampleCount()].newClusterSize();
            var bSize = this.clustering.steps()[b - this.clustering.exampleCount()].newClusterSize();
            return aSize - bSize;
        });

        var visitedLeafs = new boolean[this.clustering.exampleCount()];
        var order = new int[this.clustering.exampleCount()];
        var orderIndex = 0;

        while (!stack.empty()) {
            int clusterIndex = stack.pop();
            if (clusterIndex < this.clustering.exampleCount()) {
                if (!visitedLeafs[clusterIndex]) {
                    order[orderIndex++] = clusterIndex;
                    visitedLeafs[clusterIndex] = true;
                }

                continue;
            }

            pushClusterChildrenInOrder(stack, clusterIndex);
        }

        for (int i = 0; i < this.clustering.exampleCount(); ++i) {
            if (!visitedLeafs[i]) {
                order[orderIndex++] = i;
            }
        }

        return order;
    }

    /**
     * Inserisce nello {@code stack}, specificato come parametro, i figli del cluster con indice {@code clusterIndex},
     * specificato come parametro, in ordine della loro dimensione.
     *
     * @param stack lo stack dove si vogliono inserire i figli di {@code clusterIndex}
     * @param clusterIndex l'indice del cluster del quale si vogliono inserire i figli nello {@code stack}
     */
    private void pushClusterChildrenInOrder(Stack<Integer> stack, int clusterIndex) {
        var createStep = this.clustering.steps()[clusterIndex - this.clustering.exampleCount()];

        int firstClusterSize = getClusterSize(createStep.firstClusterIndex());
        int secondClusterSize = getClusterSize(createStep.secondClusterIndex());

        if (firstClusterSize < secondClusterSize) {
            stack.push(createStep.firstClusterIndex());
            stack.push(createStep.secondClusterIndex());
        } else {
            stack.push(createStep.secondClusterIndex());
            stack.push(createStep.firstClusterIndex());
        }
    }

    /**
     * Restituisce la dimensione del cluster con indice {@code clusterIndex}, specificato come parametro.
     *
     * @param clusterIndex l'indice del cluster di cui si vuole conoscere la dimensione
     * @return la dimensione del cluster con indice {@code clusterIndex}
     */
    private int getClusterSize(int clusterIndex) {
        if (clusterIndex < this.clustering.exampleCount()) {
            return 1;
        }

        return this.clustering.steps()[clusterIndex - this.clustering.exampleCount()].newClusterSize();
    }

    /**
     * Restituisce l'orientazione del dendrogramma.
     *
     * @return l'orientazione del dendrogramma
     */
    Orientation getOrientation() {
        return this.orientation;
    }

    /**
     * Scambia l'orientazione del dendrogramma.
     */
    void swapOrientation() {
        this.orientation = switch (orientation) {
            case HORIZONTAL -> Orientation.VERTICAL;
            case VERTICAL -> Orientation.HORIZONTAL;
        };

        var transform = AffineTransform.getRotateInstance(this.orientation == Orientation.VERTICAL ? Math.PI / 2 : -Math.PI / 2);
        transform.transform(this.clusterPositions, 0, this.clusterPositions, 0, this.clusterPositions.length);
    }

    /**
     * Restituisce il numero di cluster del dendrogramma.
     *
     * @return il numero di cluster del dendrogramma
     */
    int getClusterCount() {
        return this.clusterPositions.length;
    }

    /**
     * Restituisce la posizione del cluster nel dendrogramma con indice {@code clusterIndex}, specificato come
     * parametro.
     *
     * @param clusterIndex l'indice del cluster di cui si vuole conoscere la posizione
     * @return la posizione del cluster con indice {@code clusterIndex}
     */
    Point getClusterPosition(int clusterIndex) {
        return this.clusterPositions[clusterIndex];
    }

    /**
     * Restituisce il numero di connessione tra cluster nel dendrogramma.
     *
     * @return il numero di connessione tra cluster nel dendrogramma
     */
    int getEdgeCount() {
        return this.edges.length;
    }

    /**
     * Restituisce la connessione con indice {@code edgeIndex} nel dendrogramma, specificato come parametro.
     *
     * @param edgeIndex l'indice della connessione
     * @return la connessione con indice {@code edgeIndex}
     */
    int[] getEdge(int edgeIndex) {
        return this.edges[edgeIndex];
    }

    /**
     * Restituisce gli indici degli esempi contenuti nel cluster con indice {@code clusterIndex}, specificato come parametro.
     *
     * @param clusterIndex l'indice del cluster di cui si vuole conoscere la lista degli esempi contenuti al suo interno
     * @return la lista degli indici degli esempi contenuti nel cluster con indice {@code clusterIndex}
     */
    List<Integer> getExamplesInCluster(int clusterIndex) {
        var exampleIndices = new ArrayList<Integer>();
        var stack = new Stack<Integer>();
        stack.push(clusterIndex);

        while (!stack.empty()) {
            int index = stack.pop();
            if (index < this.clustering.exampleCount()) {
                exampleIndices.add(index);
                continue;
            }

            var createStep = this.clustering.steps()[index - this.clustering.exampleCount()];

            stack.add(createStep.firstClusterIndex());
            stack.add(createStep.secondClusterIndex());
        }

        exampleIndices.sort(Integer::compareTo);
        return exampleIndices;
    }
}

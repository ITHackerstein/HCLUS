package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.common.Clustering;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

class Dendrogram {
    static final int CLUSTER_SIZE = 16;
    static final int CLUSTER_SPACING = 8;
    static final int CLUSTER_BORDER_SIZE = 2;
    static final int EDGE_LINE_WIDTH = 1;
    static final Color CLUSTER_COLOR = new Color(138, 43, 216);
    static final Color EDGE_COLOR = Color.black;
    static final Color TOOLTIP_BACKGROUND_COLOR = new Color(200, 200, 200);
    static final int TOOLTIP_PADDING = 5;
    static final int TOOLTIP_BORDER_SIZE = 2;
    static final double TOOLTIP_HEIGHT_PERCENTAGE = 0.2;
    static final int TOOLTIP_SCROLLBAR_HEIGHT = 12;
    static final int TOOLTIP_SCROLLBAR_WIDTH = 2;

    private final Clustering clustering;
    private final Point[] clusterPositions;
    private final int[][] edges;

    Dendrogram(Clustering clustering) {
        this.clustering = clustering;
        this.clusterPositions = new Point[this.clustering.exampleCount() + this.clustering.steps().length];
        this.edges = new int[this.clusterPositions.length * 2][2];
        this.initializeArrays();
    }

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

    private void reorderLeafs() {
        int[] order;
        if (this.clustering.exampleCount() == this.clustering.steps().length + 1) {
            order = findLeafOrderForCompleteDendrogram();
        } else {
            order = findLeafOrderForPartialDendrogram();
        }

        var oldClusterPositions = Arrays.copyOf(this.clusterPositions, this.clusterPositions.length);
        for (int i = 0; i < this.clusterPositions.length; ++i) {
            if (i < order.length && order[i] != -1) {
                this.clusterPositions[order[i]] = oldClusterPositions[i];
            } else {
                this.clusterPositions[i] = oldClusterPositions[i];
            }
        }
    }

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

    private int getClusterSize(int clusterIndex) {
        if (clusterIndex < this.clustering.exampleCount()) {
            return 1;
        }

        return this.clustering.steps()[clusterIndex - this.clustering.exampleCount()].newClusterSize();
    }

    int getClusterCount() {
        return this.clusterPositions.length;
    }

    Point getClusterPosition(int clusterIndex) {
        return this.clusterPositions[clusterIndex];
    }

    int getEdgeCount() {
        return this.edges.length;
    }

    int[] getEdge(int edgeIndex) {
        return this.edges[edgeIndex];
    }

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

package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.common.Clustering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Stack;

public class DendrogramViewerWidget extends JPanel implements MouseMotionListener {
    private class Dendrogram {
        private int clusterSize = 32;
        private int clusterSpacing = 16;
        private int clusterBorderSize = 6;
        private int edgeLineWidth = 1;
        private Color clusterColor = new Color(138, 43, 216);
        private Color edgeColor = Color.black;

        private int[] leafClusterIndexMap;
        private Point[] clusterPositions;
        private int[][] edges;

        public Dendrogram() {
            this.leafClusterIndexMap = new int[clustering.exampleCount()];
            this.clusterPositions = new Point[clustering.exampleCount() + clustering.steps().length];
            this.edges = new int[this.clusterPositions.length * 2][2];
            this.initializeArrays();
        }

        private int getActualClusterIndex(int clusterIndex) {
            var result = clusterIndex;
            if (result < clustering.exampleCount()) {
                result = this.leafClusterIndexMap[result];
            }
            return result;
        }

        private void fillCenteredCircle(Graphics2D g2d, int x, int y, int radius) {
            g2d.fillArc(x - radius, y - radius, radius * 2, radius * 2, 0, 360);
        }

        private void initializeArrays() {
            var stack = new Stack<Integer>();
            stack.push(this.clusterPositions.length - 1);

            int leafIndex = 0;
            int edgeIndex = 0;
            while (!stack.empty()) {
                var clusterIndex = stack.pop();
                if (clusterIndex < clustering.exampleCount()) {
                    this.leafClusterIndexMap[clusterIndex] = leafIndex;
                    clusterPositions[leafIndex] = new Point(
                        canvasRectangle.width / 2 + (this.clusterSpacing + this.clusterSize * 2) * (leafIndex - clustering.exampleCount() / 2),
                        canvasRectangle.height / 2
                    );

                    ++leafIndex;
                    continue;
                }

                var createStep = clustering.steps()[clusterIndex - clustering.exampleCount()];

                stack.push(createStep.firstClusterIndex());
                stack.push(createStep.secondClusterIndex());

                edges[edgeIndex][0] = clusterIndex;
                edges[edgeIndex][1] = createStep.firstClusterIndex();
                ++edgeIndex;
                edges[edgeIndex][0] = clusterIndex;
                edges[edgeIndex][1] = createStep.secondClusterIndex();
                ++edgeIndex;
            }

            for (int i = 0; i < clustering.steps().length; ++i) {
                var step = clustering.steps()[i];
                var firstClusterPosition = clusterPositions[getActualClusterIndex(step.firstClusterIndex())];
                var secondClusterPosition = clusterPositions[getActualClusterIndex(step.secondClusterIndex())];
                clusterPositions[clustering.exampleCount() + i] = new Point(
                    (int) ((firstClusterPosition.getX() + secondClusterPosition.getX()) / 2),
                    (int) (Math.min(firstClusterPosition.getY(), secondClusterPosition.getY()) - this.clusterSpacing - this.clusterSize * 2)
                );
            }
        }

        public void paint(Graphics2D g2d) {
            for (var edge : this.edges) {
                var firstIndex = getActualClusterIndex(edge[0]);
                var secondIndex = getActualClusterIndex(edge[1]);

                g2d.setColor(this.edgeColor);
                g2d.setStroke(new BasicStroke(this.edgeLineWidth));
                g2d.drawLine(
                    clusterPositions[firstIndex].x, clusterPositions[firstIndex].y,
                    clusterPositions[secondIndex].x, clusterPositions[secondIndex].y
                );
            }

            for (var clusterPosition : clusterPositions) {
                var distance = mousePosition.distance(clusterPosition);
                System.out.println(distance);
                var color1 = this.clusterColor.darker();
                var color2 = this.clusterColor;
                if (distance <= this.clusterSize) {
                    color1 = color1.brighter();
                    color2 = color2.brighter();
                }

                g2d.setColor(color1);
                fillCenteredCircle(g2d, clusterPosition.x, clusterPosition.y, this.clusterSize);
                g2d.setColor(color2);
                fillCenteredCircle(g2d, clusterPosition.x, clusterPosition.y, this.clusterSize - this.clusterBorderSize);
            }

            System.out.println();
        }
    }

    private Clustering clustering = null;
    private Dendrogram dendrogram = null;
    private Rectangle canvasRectangle = new Rectangle();

    private Point mousePosition = new Point();

    public DendrogramViewerWidget() {
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dendrogramma"));
        this.addMouseMotionListener(this);
    }

    public void setClustering(Clustering clustering) {
        this.clustering = clustering;
        this.dendrogram = new Dendrogram();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(640, 640);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        var g2d = (Graphics2D) g;

        var insets = this.getBorder().getBorderInsets(this);
        this.canvasRectangle = new Rectangle(insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (this.clustering == null || !this.isEnabled()) {
            g2d.setFont(g2d.getFont().deriveFont(18.0f));

            var text = "Nessun dendrogramma caricato!";
            var metrics = g2d.getFontMetrics(g2d.getFont());
            var x = (int) canvasRectangle.getCenterX() - metrics.stringWidth(text) / 2;
            var y = (int) canvasRectangle.getCenterY() - metrics.getHeight() / 2;

            g2d.setColor(new Color(0, 0, 0, 40));
            g2d.fillRect(canvasRectangle.x, canvasRectangle.y, canvasRectangle.width, canvasRectangle.height);
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, x, y);
            return;
        }

        g2d.clearRect(this.canvasRectangle.x, this.canvasRectangle.y, this.canvasRectangle.width, this.canvasRectangle.height);
        g2d.setClip(this.canvasRectangle.x, this.canvasRectangle.y, this.canvasRectangle.width, this.canvasRectangle.height);

        this.dendrogram.paint(g2d);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        this.mousePosition = event.getPoint();
        repaint();
    }
}

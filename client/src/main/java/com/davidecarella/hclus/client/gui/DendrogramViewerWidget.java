package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.client.communication.ServerConnection;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.Example;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Stack;

class Dendrogram {
    static final int CLUSTER_SIZE = 32;
    static final int CLUSTER_SPACING = 16;
    static final int CLUSTER_BORDER_SIZE = 6;
    static final int EDGE_LINE_WIDTH = 1;
    static final Color CLUSTER_COLOR = new Color(138, 43, 216);
    static final Color EDGE_COLOR = Color.black;
    static final Color TOOLTIP_BACKGROUND_COLOR = new Color(200, 200, 200);
    static final int TOOLTIP_PADDING = 8;
    static final int TOOLTIP_BORDER_SIZE = 2;

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
        var stack = new Stack<Integer>();
        stack.push(this.clusterPositions.length - 1);

        int offset = 0;
        int edgeIndex = 0;
        while (!stack.empty()) {
            var clusterIndex = stack.pop();
            if (clusterIndex < this.clustering.exampleCount()) {
                this.clusterPositions[clusterIndex] = new Point((CLUSTER_SPACING + CLUSTER_SIZE * 2) * (offset - this.clustering.exampleCount() / 2), 0);
                ++offset;
                continue;
            }

            var createStep = this.clustering.steps()[clusterIndex - this.clustering.exampleCount()];

            stack.push(createStep.firstClusterIndex());
            stack.push(createStep.secondClusterIndex());

            this.edges[edgeIndex][0] = clusterIndex;
            this.edges[edgeIndex][1] = createStep.firstClusterIndex();
            ++edgeIndex;

            this.edges[edgeIndex][0] = clusterIndex;
            this.edges[edgeIndex][1] = createStep.secondClusterIndex();
            ++edgeIndex;
        }

        for (int i = 0; i < this.clustering.steps().length; ++i) {
            var step = this.clustering.steps()[i];
            var firstClusterPosition = this.clusterPositions[step.firstClusterIndex()];
            var secondClusterPosition = this.clusterPositions[step.secondClusterIndex()];
            this.clusterPositions[this.clustering.exampleCount() + i] = new Point(
                (int) ((firstClusterPosition.getX() + secondClusterPosition.getX()) / 2),
                (int) (Math.min(firstClusterPosition.getY(), secondClusterPosition.getY()) - CLUSTER_SPACING - CLUSTER_SIZE * 2)
            );
        }
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

public class DendrogramViewerWidget extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Dendrogram dendrogram = null;
    private int selectedClusterIndex = -1;
    private int hoveredClusterIndex = -1;
    private List<Integer> selectedClusterExampleIndices = null;
    private List<Example> selectedClusterExamples = null;
    private String clusterTooltipError = null;

    private AffineTransform transform = null;

    private Point lastMousePosition = new Point();
    private int pressedMouseButtons = 0;

    public DendrogramViewerWidget() {
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dendrogramma"));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }

    public void setClustering(Clustering clustering) {
        this.dendrogram = new Dendrogram(clustering);
        this.repaint();
    }

    private Point inverseTransformPoint(Point point) {
        Point result = new Point();
        try {
            this.transform.inverseTransform(point, result);
        } catch (NoninvertibleTransformException ignored) {
            // NOTE: This can never happen because we construct the matrix only using `translate`, `scale` and the
            //       multiplication operator, so it is always invertible
        }

        return result;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(640, 640);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2d = (Graphics2D) g;

        var insets = this.getBorder().getBorderInsets(this);
        var canvasRectangle = new Rectangle(insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (this.dendrogram == null || !this.isEnabled()) {
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

        if (this.transform == null) {
            this.transform = AffineTransform.getTranslateInstance(canvasRectangle.width / 2.0, canvasRectangle.height / 2.0);
        }

        var oldClip = g2d.getClip();
        g2d.setClip(canvasRectangle.x, canvasRectangle.y, canvasRectangle.width, canvasRectangle.height);
        g2d.clearRect(canvasRectangle.x, canvasRectangle.y, canvasRectangle.width, canvasRectangle.height);
        g2d.setTransform(this.transform);

        for (int i = 0; i < this.dendrogram.getEdgeCount(); ++i) {
            var edge = this.dendrogram.getEdge(i);
            var firstPosition = this.dendrogram.getClusterPosition(edge[0]);
            var secondPosition = this.dendrogram.getClusterPosition(edge[1]);

            g2d.setColor(Dendrogram.EDGE_COLOR);
            g2d.setStroke(new BasicStroke(Dendrogram.EDGE_LINE_WIDTH));
            g2d.drawLine(firstPosition.x, firstPosition.y, secondPosition.x, secondPosition.y);
        }

        for (int i = 0; i < this.dendrogram.getClusterCount(); ++i) {
            var clusterPosition = this.dendrogram.getClusterPosition(i);

            var color1 = Dendrogram.CLUSTER_COLOR.darker();
            var color2 = Dendrogram.CLUSTER_COLOR;
            if (this.selectedClusterIndex == i || this.hoveredClusterIndex == i)
            {
                color1 = color1.brighter();
                color2 = color2.brighter();
            }

            g2d.setColor(color1);
            fillCenteredCircle(g2d, clusterPosition.x, clusterPosition.y, Dendrogram.CLUSTER_SIZE);
            g2d.setColor(color2);
            fillCenteredCircle(g2d, clusterPosition.x, clusterPosition.y, Dendrogram.CLUSTER_SIZE - Dendrogram.CLUSTER_BORDER_SIZE);
        }

        g2d.setTransform(new AffineTransform());

        var normalFont = g2d.getFont();
        var normalFontMetrics = g2d.getFontMetrics(normalFont);
        var boldFont = normalFont.deriveFont(Font.BOLD);
        var boldFontMetrics = g2d.getFontMetrics(boldFont);

        if (this.clusterTooltipError != null) {
            var width = normalFontMetrics.stringWidth(this.clusterTooltipError);
            var height = normalFontMetrics.getHeight();

            var borderRectangle = getTooltipRectangleWithBorder(canvasRectangle, width, height);
            var rectangle = getTooltipRectangle(canvasRectangle, width, height);

            g2d.setColor(Dendrogram.TOOLTIP_BACKGROUND_COLOR.darker());
            g2d.fill(borderRectangle);

            g2d.setColor(Dendrogram.TOOLTIP_BACKGROUND_COLOR);
            g2d.fill(rectangle);

            g2d.setColor(Color.RED);
            g2d.drawString(
                this.clusterTooltipError,
                rectangle.x + Dendrogram.TOOLTIP_PADDING, rectangle.y + normalFontMetrics.getHeight()
            );
        } else if (this.selectedClusterExampleIndices != null && this.selectedClusterExamples != null) {
            var title = "Esempi nel cluster selezionato";

            var width = boldFontMetrics.stringWidth(title);
            var height = boldFontMetrics.getHeight();

            for (int i = 0; i < this.selectedClusterExampleIndices.size(); ++i) {
                var exampleString = String.format("%d: %s", this.selectedClusterExampleIndices.get(i), this.selectedClusterExamples.get(i));
                width = Math.max(width, normalFontMetrics.stringWidth(exampleString));
                height += normalFontMetrics.getHeight();
            }

            var borderRectangle = getTooltipRectangleWithBorder(canvasRectangle, width, height);
            var rectangle = getTooltipRectangle(canvasRectangle, width, height);

            g2d.setColor(Dendrogram.TOOLTIP_BACKGROUND_COLOR.darker());
            g2d.fill(borderRectangle);

            g2d.setColor(Dendrogram.TOOLTIP_BACKGROUND_COLOR);
            g2d.fill(rectangle);

            var x = rectangle.x + Dendrogram.TOOLTIP_PADDING;
            var y = rectangle.y + boldFontMetrics.getHeight();

            g2d.setColor(Color.BLACK);
            g2d.setFont(boldFont);
            g2d.drawString(title, x, y);
            g2d.setFont(normalFont);
            for (int i = 0; i < this.selectedClusterExamples.size(); ++i) {
                y += normalFontMetrics.getHeight();
                var exampleString = String.format("%d: %s", this.selectedClusterExampleIndices.get(i), this.selectedClusterExamples.get(i));
                g2d.drawString(exampleString, x, y);
            }
        }

        g2d.setClip(oldClip);
    }

    private void fillCenteredCircle(Graphics2D g2d, int x, int y, int radius) {
        g2d.fillArc(x - radius, y - radius, radius * 2, radius * 2, 0, 360);
    }

    private Rectangle getTooltipRectangleWithBorder(Rectangle canvasRectangle, int width, int height) {
        var widthWithBorderAndPadding = width + (Dendrogram.TOOLTIP_PADDING + Dendrogram.TOOLTIP_BORDER_SIZE) * 2;
        var heightWithBorderAndPadding = height + (Dendrogram.TOOLTIP_PADDING + Dendrogram.TOOLTIP_BORDER_SIZE) * 2;

        return new Rectangle(
            canvasRectangle.x + canvasRectangle.width - widthWithBorderAndPadding,
            canvasRectangle.y + canvasRectangle.height - heightWithBorderAndPadding,
            widthWithBorderAndPadding,
            heightWithBorderAndPadding
        );
    }

    private Rectangle getTooltipRectangle(Rectangle canvasRectangle, int width, int height) {
        var widthWithPadding = width + Dendrogram.TOOLTIP_PADDING * 2;
        var heightWithPadding = height + Dendrogram.TOOLTIP_PADDING * 2;

        return new Rectangle(
            canvasRectangle.x + canvasRectangle.width - widthWithPadding - Dendrogram.TOOLTIP_BORDER_SIZE,
            canvasRectangle.y + canvasRectangle.height - heightWithPadding - Dendrogram.TOOLTIP_BORDER_SIZE,
            widthWithPadding,
            heightWithPadding
        );
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (this.dendrogram == null || !this.isEnabled() || this.pressedMouseButtons != MouseEvent.BUTTON2) {
            return;
        }

        if (this.lastMousePosition != null) {
            this.transform.translate(event.getX() - this.lastMousePosition.getX(), event.getY() - this.lastMousePosition.getY());
        }

        this.lastMousePosition = event.getPoint();
        this.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        this.lastMousePosition = event.getPoint();

        if (this.dendrogram == null || !this.isEnabled() || this.pressedMouseButtons != MouseEvent.NOBUTTON) {
            return;
        }

        var mousePosition = this.inverseTransformPoint(event.getPoint());
        for (int i = 0; i < this.dendrogram.getClusterCount(); ++i) {
            var clusterPosition = this.dendrogram.getClusterPosition(i);
            if (mousePosition.distance(clusterPosition) <= Dendrogram.CLUSTER_SIZE) {
                if (i == this.hoveredClusterIndex) {
                    return;
                }

                this.hoveredClusterIndex = i;
                this.repaint();
                return;
            }
        }

        this.hoveredClusterIndex = -1;
        this.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (this.dendrogram == null || !this.isEnabled() || event.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        var mousePosition = this.inverseTransformPoint(event.getPoint());
        for (int i = 0; i < this.dendrogram.getClusterCount(); ++i) {
            var clusterPosition = this.dendrogram.getClusterPosition(i);
            if (mousePosition.distance(clusterPosition) <= Dendrogram.CLUSTER_SIZE) {
                if (i == this.selectedClusterIndex) {
                    return;
                }

                this.selectedClusterIndex = i;
                var exampleIndices = this.dendrogram.getExamplesInCluster(this.selectedClusterIndex);
                try {
                    this.selectedClusterExampleIndices = exampleIndices;
                    this.selectedClusterExamples = ServerConnection.the().getExamples(this.selectedClusterExampleIndices);
                    this.clusterTooltipError = null;
                } catch (IOException exception) {
                    this.selectedClusterExampleIndices = null;
                    this.selectedClusterExamples = null;
                    this.clusterTooltipError = String.format("Errore durante il caricamento degli esempi: %s!", exception.getMessage());
                }

                this.repaint();
                return;
            }
        }

        this.selectedClusterIndex = -1;
        this.repaint();
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (this.dendrogram == null || !this.isEnabled()) {
            return;
        }

        this.pressedMouseButtons |= event.getButton();
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (this.dendrogram == null || !this.isEnabled()) {
            return;
        }

        this.pressedMouseButtons &= ~event.getButton();
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        if (this.dendrogram == null || !this.isEnabled()) {
            return;
        }

        int notches = event.getWheelRotation();
        var zoom = Math.exp(-Math.signum(notches) * 0.02);

        var newTransform = new AffineTransform();
        newTransform.translate(event.getX(), event.getY());
        newTransform.scale(zoom, zoom);
        newTransform.translate(-event.getX(), -event.getY());
        newTransform.concatenate(this.transform);
        this.transform = newTransform;

        this.repaint();
    }
}

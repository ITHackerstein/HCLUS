package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.client.communication.ServerConnection;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.Example;

import javax.swing.*;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;

public class DendrogramViewerWidget extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {
    private Dendrogram dendrogram = null;
    private int selectedClusterIndex = -1;
    private int hoveredClusterIndex = -1;
    private List<Integer> selectedClusterExampleIndices = null;
    private List<Example> selectedClusterExamples = null;
    private int selectedClusterFirstExampleIndex = -1;
    private String clusterTooltipError = null;

    private AffineTransform transform = null;

    private Point lastMousePosition = new Point();
    private int pressedMouseButtons = 0;

    public DendrogramViewerWidget() {
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dendrogramma"));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addComponentListener(this);
    }

    public void setClustering(Clustering clustering) {
        this.dendrogram = clustering == null ? null : new Dendrogram(clustering);
        this.selectedClusterIndex = -1;
        this.hoveredClusterIndex = -1;
        this.selectedClusterExampleIndices = null;
        this.selectedClusterExamples = null;
        this.selectedClusterFirstExampleIndex = -1;
        this.clusterTooltipError = null;
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
            g2d.drawLine(firstPosition.x, firstPosition.y, secondPosition.x, firstPosition.y);
            g2d.drawLine(secondPosition.x, firstPosition.y, secondPosition.x, secondPosition.y);
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
            for (int i = 0; i < this.selectedClusterExampleIndices.size(); ++i) {
                var exampleString = String.format("%d: %s", this.selectedClusterExampleIndices.get(i), this.selectedClusterExamples.get(i));
                width = Math.max(width, normalFontMetrics.stringWidth(exampleString));
            }

            var height = boldFontMetrics.getHeight() + normalFontMetrics.getHeight() * this.selectedClusterExampleIndices.size();
            height = (int) Math.min(height, canvasRectangle.height * Dendrogram.TOOLTIP_HEIGHT_PERCENTAGE);

            var borderRectangle = getTooltipRectangleWithBorder(canvasRectangle, width, height);
            var rectangle = getTooltipRectangle(canvasRectangle, width, height);

            g2d.setColor(Dendrogram.TOOLTIP_BACKGROUND_COLOR.darker());
            g2d.fill(borderRectangle);

            g2d.setColor(Dendrogram.TOOLTIP_BACKGROUND_COLOR);
            g2d.fill(rectangle);

            var exampleCount = (height - boldFontMetrics.getHeight()) / normalFontMetrics.getHeight();
            if (exampleCount == this.selectedClusterExamples.size()) {
                this.selectedClusterFirstExampleIndex = 0;
            } else {
                this.selectedClusterFirstExampleIndex = Math.clamp(this.selectedClusterFirstExampleIndex, 0, this.selectedClusterExamples.size() - exampleCount);

                var scrollPercentage = ((double) this.selectedClusterFirstExampleIndex) / (this.selectedClusterExamples.size() - exampleCount);
                var scrollbarY = rectangle.y + boldFontMetrics.getHeight() + scrollPercentage * (rectangle.height - boldFontMetrics.getHeight() - Dendrogram.TOOLTIP_SCROLLBAR_HEIGHT);

                g2d.setColor(Color.GRAY);
                g2d.fillRect(
                    rectangle.x + rectangle.width - Dendrogram.TOOLTIP_SCROLLBAR_WIDTH, (int) scrollbarY,
                    Dendrogram.TOOLTIP_SCROLLBAR_WIDTH, Dendrogram.TOOLTIP_SCROLLBAR_HEIGHT
                );
            }

            var x = rectangle.x + Dendrogram.TOOLTIP_PADDING;
            var y = rectangle.y + boldFontMetrics.getHeight();

            g2d.setColor(Color.BLACK);
            g2d.setFont(boldFont);
            g2d.drawString(title, x, y);
            g2d.setFont(normalFont);
            for (int i = this.selectedClusterFirstExampleIndex; i < this.selectedClusterFirstExampleIndex + exampleCount; ++i) {
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
            var mousePosition = this.inverseTransformPoint(event.getPoint());
            var lastMousePosition = this.inverseTransformPoint(this.lastMousePosition);
            this.transform.translate(mousePosition.getX() - lastMousePosition.getX(), mousePosition.getY() - lastMousePosition.getY());
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
                    this.selectedClusterIndex = -1;
                    this.selectedClusterExampleIndices = null;
                    this.selectedClusterExamples = null;
                    this.selectedClusterFirstExampleIndex = -1;
                    this.clusterTooltipError = null;
                    return;
                }

                this.selectedClusterIndex = i;
                var exampleIndices = this.dendrogram.getExamplesInCluster(this.selectedClusterIndex);
                try {
                    this.selectedClusterExampleIndices = exampleIndices;
                    this.selectedClusterExamples = ServerConnection.the().getExamples(this.selectedClusterExampleIndices);
                    this.selectedClusterFirstExampleIndex = 0;
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
        this.selectedClusterExampleIndices = null;
        this.selectedClusterExamples = null;
        this.selectedClusterFirstExampleIndex = -1;
        this.clusterTooltipError = null;
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

        if ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
            int notches = event.getWheelRotation();
            var zoom = Math.exp(-Math.signum(notches) * 0.02);

            var newTransform = new AffineTransform();
            newTransform.translate(event.getX(), event.getY());
            newTransform.scale(zoom, zoom);
            newTransform.translate(-event.getX(), -event.getY());
            newTransform.concatenate(this.transform);
            this.transform = newTransform;
        } else {
            this.selectedClusterFirstExampleIndex += (int) Math.signum(event.getWheelRotation());
        }


        this.repaint();
    }

    @Override
    public void componentResized(ComponentEvent event) {
        this.repaint();
    }

    @Override
    public void componentMoved(ComponentEvent event) {
        this.repaint();
    }

    @Override
    public void componentShown(ComponentEvent event) {
        this.repaint();
    }

    @Override
    public void componentHidden(ComponentEvent event) {
        this.repaint();
    }
}

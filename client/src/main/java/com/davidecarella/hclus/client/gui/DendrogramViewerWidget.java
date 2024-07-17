package com.davidecarella.hclus.client.gui;

import com.davidecarella.hclus.client.communication.ServerConnection;
import com.davidecarella.hclus.client.exceptions.ServerException;
import com.davidecarella.hclus.common.Clustering;
import com.davidecarella.hclus.common.Example;

import javax.swing.*;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;

/**
 * <p>Widget per la visualizzazione di un dendrogramma associato a un clustering.
 *
 * <p>Per ogni cluster viene mostrato un cerchio colorato da cui partono le eventuali connessioni verso altri cluster,
 * ognuno di questi cluster può essere cliccato con il tasto sinistro del mouse e verrà mostrato un tooltip che contiene
 * la lista degli esempi contenuti in esso.
 *
 * <p>L'utente può navigare il dendrogramma spostandolo (tenendo premuto la rotellina del mouse e muovendolo) e zoomando
 * (tenendo premuto {@code CTRL} sulla tastiera e scorrendo con la rotellina del mouse).
 *
 * @see Dendrogram
 */
class DendrogramViewerWidget extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {
    /**
     * Il dendrogramma che si sta visualizzando.
     */
    private Dendrogram dendrogram = null;

    /**
     * L'eventuale indice del cluster selezionato dall'utente.
     */
    private int selectedClusterIndex = -1;

    /**
     * L'eventuale indice del cluster su cui si trova il mouse.
     */
    private int hoveredClusterIndex = -1;

    /**
     * L'eventuale lista degli indici degli esempi contenuti nel cluster selezionato.
     */
    private List<Integer> selectedClusterExampleIndices = null;

    /**
     * L'eventuale lista degli esempi contenuti nel cluster selezionato.
     */
    private List<Example> selectedClusterExamples = null;

    /**
     * L'eventuale indice del primo esempio che si sta visualizzando nel tooltip del cluster selezionato.
     */
    private int selectedClusterFirstExampleIndex = -1;

    /**
     * L'eventuale errore mostrato nel tooltip del cluster selezionato.
     */
    private String clusterTooltipError = null;

    /**
     * La matrice di trasformazione dell'area di visualizzazione del dendrogramma.
     */
    private AffineTransform transform = null;

    /**
     * La posizione precedente del mouse.
     */
    private Point lastMousePosition = new Point();

    /**
     * La bitmask dei pulsanti del mouse premuti.
     */
    private int pressedMouseButtons = 0;

    /**
     * Costruisce il widget per la visualizzazione del dendrogramma.
     */
    DendrogramViewerWidget() {
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dendrogramma"));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addComponentListener(this);
    }

    /**
     * Imposta il clustering di cui si vuole visualizzare il dendrogramma.
     *
     * @param clustering il clustering di cui si vuole visualizzare il dendrogramma
     */
    void setClustering(Clustering clustering) {
        this.dendrogram = clustering == null ? null : new Dendrogram(clustering);
        this.selectedClusterIndex = -1;
        this.hoveredClusterIndex = -1;
        this.selectedClusterExampleIndices = null;
        this.selectedClusterExamples = null;
        this.selectedClusterFirstExampleIndex = -1;
        this.clusterTooltipError = null;
        this.repaint();
    }

    /**
     * Applica l'inversa della matrice di trasformazione al punto {@code point}, specificato come parametro.
     *
     * @param point il punto su cui si vuole applicare l'inversa della matrice di trasformazione
     * @return il punto trasformato
     */
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
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.drawString(text, x, y);
            return;
        }

        if (this.transform == null) {
            this.transform = AffineTransform.getTranslateInstance(canvasRectangle.width / 2.0, canvasRectangle.height / 2.0);
        }

        g2d.setClip(canvasRectangle.x, canvasRectangle.y, canvasRectangle.width, canvasRectangle.height);
        g2d.clearRect(canvasRectangle.x, canvasRectangle.y, canvasRectangle.width, canvasRectangle.height);
        g2d.setTransform(this.transform);

        for (int i = 0; i < this.dendrogram.getEdgeCount(); ++i) {
            var edge = this.dendrogram.getEdge(i);
            var firstPosition = this.dendrogram.getClusterPosition(edge[0]);
            var secondPosition = this.dendrogram.getClusterPosition(edge[1]);

            g2d.setColor(Dendrogram.EDGE_COLOR);
            g2d.setStroke(new BasicStroke(Dendrogram.EDGE_LINE_WIDTH));
            if (dendrogram.getOrientation() == Dendrogram.Orientation.HORIZONTAL) {
                g2d.drawLine(firstPosition.x, firstPosition.y, firstPosition.x, secondPosition.y);
                g2d.drawLine(firstPosition.x, secondPosition.y, secondPosition.x, secondPosition.y);
            } else {
                g2d.drawLine(firstPosition.x, firstPosition.y, secondPosition.x, firstPosition.y);
                g2d.drawLine(secondPosition.x, firstPosition.y, secondPosition.x, secondPosition.y);
            }
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
    }

    /**
     * Mostra un cerchio con centro in posizione con posizione {@code (x, y)} e raggio {@code radius}, specificati come
     * parametro.
     *
     * @param g2d il contesto per il rendering 2D
     * @param x l'ascissa del centro del cerchio
     * @param y l'ordinata del centro del cerchio
     * @param radius il raggio del cerchio
     */
    private void fillCenteredCircle(Graphics2D g2d, int x, int y, int radius) {
        g2d.fillArc(x - radius, y - radius, radius * 2, radius * 2, 0, 360);
    }

    /**
     * Restituisce il rettangolo del tooltip del cluster selezionato, includendo il bordo.
     *
     * @param canvasRectangle il rettangolo dell'area di visualizzazione
     * @param width la larghezza del contenuto del tooltip
     * @param height l'altezza del contenuto del tooltip
     * @return il rettangolo del tooltip del cluster selezionato, includendo il bordo
     */
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

    /**
     * Restituisce il rettangolo del tooltip del cluster selezionato, senza il bordo.
     *
     * @param canvasRectangle il rettangolo dell'area di visualizzazione
     * @param width la larghezza del contenuto del tooltip
     * @param height l'altezza del contenuto del tooltip
     * @return il rettangolo del tooltip del cluster selezionato, senza il bordo
     */
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
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void keyPressed(KeyEvent event) {
    }

    /**
     * <p>Gestisce l'evento del rilascio di un tasto della tastiera.
     *
     * <p>Controlla se viene rilasciata la {@code R} sulla tastiera e in tal caso ruota il dendrogramma.
     *
     * @param event l'evento da processare
     */
    @Override
    public void keyReleased(KeyEvent event) {
        if (event.getKeyChar() != 'R' && event.getKeyChar() != 'r') {
            return;
        }

        this.dendrogram.swapOrientation();
        repaint();
    }

    /**
     * <p>Gestisce l'evento di <i>dragging</i> del mouse mentre si sta tenendo premuta la rotellina del mouse.
     *
     * <p>In particolare effettua una traslazione sulla matrice di trasformazione con una quantità pari a quella
     * dello spostamento del mouse.
     *
     * @param event l'evento da processare
     */
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

    /**
     * <p>Gestisce l'evento di spostamento del mouse.
     *
     * <p>Controlla se il mouse si trova su un cluster e in tal caso lo mostra con un colore più luminoso.
     *
     * @param event l'evento da processare
     */
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

    /**
     * <p>Gestisce l'evento di spostamento del mouse.
     *
     * <p>Controlla se l'utente clicca un cluster e in tal caso lo mostra con un colore più luminoso e mostra il tooltip
     * di informazioni sul cluster selezionato.
     *
     * @param event l'evento da processare
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (this.dendrogram == null || !this.isEnabled()) {
            return;
        }

        this.requestFocus();

        if (event.getButton() != MouseEvent.BUTTON1) {
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
                } catch (ServerException exception) {
                    this.selectedClusterExampleIndices = null;
                    this.selectedClusterExamples = null;
                    this.clusterTooltipError = String.format("%s: %s!", exception.getMessage(), exception.getDetails());
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

    /**
     * Aggiorna la bitmask dei pulsanti del mouse premuti in base al pulsante premuto dall'utente.
     *
     * @param event l'evento da processare
     */
    @Override
    public void mousePressed(MouseEvent event) {
        if (this.dendrogram == null || !this.isEnabled()) {
            return;
        }

        this.pressedMouseButtons |= event.getButton();
    }

    /**
     * Aggiorna la bitmask dei pulsanti del mouse premuti in base al pulsante rilasciato dall'utente.
     *
     * @param event l'evento da processare
     */
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

    /**
     * <p>Gestisce l'evento di scorrimento della rotellina del mouse.
     *
     * <p>Se nel frattempo si tiene premuto {@code CTRL} sulla tastiera allora viene applicata una scalatura attorno
     * alla posizione del mouse, altrimenti viene scrollato il tooltip di informazioni sul cluster selezionato.
     *
     * @param event l'evento da processare
     */
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
